package pdl.backend.controller;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.imglib2.img.Img;
import org.springframework.http.HttpHeaders;
import pdl.backend.Image;
import pdl.backend.ImageConverter;

import io.scif.FormatException;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.Writer;
import io.scif.formats.JPEGFormat;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.img.SCIFIOImgPlus;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pdl.backend.*;
import pdl.backend.imageProcessing.ImgProccessing;

import javax.imageio.ImageIO;


@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  @Autowired
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> getImage(@PathVariable("id") long id) throws IOException {

    if (!imageDao.retrieve(id).isPresent()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    ByteArrayInputStream bis = new ByteArrayInputStream(imageDao.retrieve(id).get().getData());
    byte[] bytes = StreamUtils.copyToByteArray(bis);

    return ResponseEntity
            .ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(bytes);
  }

  @RequestMapping(value = "/images/{id}", params = {"algorithm"}, method = RequestMethod.GET)
  public ResponseEntity<?> ApplyEffect(@PathVariable("id") long id, @RequestParam(value="algorithm") String algo, @RequestParam(value="p1") Optional<Integer> p1,
                                       @RequestParam(required = false) Optional<Integer> p2) {

    String[] filters = {"luminosity", "histogramme", "coloredfilter", "blur", "contour", "gaussian", "glitch vertical", "glitch horizontal","negative"};
    boolean exists = false;

    for (String filter : filters) {
      if (filter.equals(algo))
        exists = true;
    }

    if (!exists) {
      return new ResponseEntity<>("Filter doesn't exist", HttpStatus.BAD_REQUEST);
    }

    if (imageDao.retrieve(id).isPresent()) {
      byte[] imgData = imageDao.retrieve(id).get().getData();
      try {
        SCIFIOImgPlus<UnsignedByteType> img = ImageConverter.imageFromJPEGBytes(imgData);
        boolean paramError = false;
        System.out.println(algo);
        switch(algo) {
          case "luminosity":
            if (p2.isEmpty()) {
              ImgProccessing.thresholdRGB(img, p1.get());
              break;
            }
            else {
              paramError = true;
              break;
            }

          case "histogramme":
            if(!p2.isEmpty()){
              ImgProccessing.dynamicContrastLUTColor(img, p1.get(), p2.get());
              break;
            }
            else {
              paramError=true;
              break;
            }
          case "coloredfilter":
            if(p2.isEmpty()){
              ImgProccessing.HueFilter(img, p1.get());
              break;
            } else {
              paramError=true;
              break;
            }
          case "blur":
            if(p2.isEmpty()){
              ImgProccessing.convolutionRGB(img, p1.get());
              break;
            } else {
              paramError=true;
              break;
            }
          case "contour":
            if (p2.isEmpty()) {
              ImgProccessing.sobel(img);
              break;
            } else {
              paramError=true;
              break;
            }
            
          case "gaussian":
            if(p2.isEmpty()){
              ImgProccessing.convolutionRGBGauss(img, p1.get());
              break;
            }
            else {
              paramError=true;
              break;
            }

          case "glitch vertical":
            if(p1.isEmpty() || p2.isEmpty()){
              ImgProccessing.RandomPixelSort(img, true);
              break;
            }
            else {
              paramError=true;
              break;
            }

          case "glitch horizontal":
            if(p1.isEmpty() || p2.isEmpty()){
              ImgProccessing.RandomPixelSort(img, false);
              break;
            }
            else {
              paramError=true;
              break;
            }

          case "negative":
            if(p1.isEmpty() || p2.isEmpty()){
              ImgProccessing.negative(img);
              break;
            }
            else {
              paramError=true;
              break;
            }

        }



        if (paramError)
          return new ResponseEntity<>("Parameters Error!", HttpStatus.BAD_REQUEST);

        imgData = ImageConverter.imageToJPEGBytes(img);

        BufferedImage _bi = ImageIO.read(new ByteArrayInputStream(imgData));
        File _image = new File("../backend/src/main/resources/images/"+id+"-"+algo+".jpg");
        ImageIO.write(_bi,"jpg",_image);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_JPEG)
                .body(imgData);

      } catch (Exception e) {
        return new ResponseEntity<>("Internal Error", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    else {
      return new ResponseEntity<>("Image not found", HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {

    if (!imageDao.retrieve(id).isPresent()) {
      return new ResponseEntity<>("Image to delete not found\n", HttpStatus.NOT_FOUND);
    }

    imageDao.delete(imageDao.retrieve(id).get());
    return new ResponseEntity<>("Image deleted successfully\n", HttpStatus.OK);
  }

  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    
    String img = file.getContentType();
    if (!img.equals(MediaType.IMAGE_JPEG.toString())) {
      return new ResponseEntity<>("Only JPEG images are supported.\n", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    try {
      imageDao.create(new Image(file.getOriginalFilename(), file.getBytes()));
    } catch (IOException e) {
      return new ResponseEntity<>("Error creating\n", HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>("Image uploaded successfully\n", HttpStatus.CREATED);
  }

  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
  @ResponseBody
  public ArrayNode getImageList() {
    ArrayNode nodes = mapper.createArrayNode();
    List<Image> imgList = imageDao.retrieveAll();

    imgList.forEach( (img) -> {
      ObjectNode imgToAdd = mapper.createObjectNode();
      imgToAdd.put("id", img.getId());
      imgToAdd.put("name", img.getName());
      imgToAdd.put("type", img.getType());
      try {
        imgToAdd.put("size", img.getSize());
      } catch (Exception e) {
        System.out.println("Error!");
      }
      nodes.add(imgToAdd);
    });
    
    return nodes;
  }

}
