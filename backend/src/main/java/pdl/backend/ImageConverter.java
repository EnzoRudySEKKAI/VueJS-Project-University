package pdl.backend;

import java.io.IOException;

import org.scijava.Context;
import org.scijava.io.location.BytesLocation;

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

public class ImageConverter {

  public static SCIFIOImgPlus<UnsignedByteType> imageFromJPEGBytes(byte[] data) throws FormatException, IOException {
    final ImgOpener imgOpener = new ImgOpener();
    final Context c = imgOpener.getContext();
    final SCIFIO scifio = new SCIFIO(c);
    final JPEGFormat format = scifio.format().getFormatFromClass(JPEGFormat.class);
    final Reader r = format.createReader();
    final BytesLocation location = new BytesLocation(data);
    r.setSource(location);
    final ArrayImgFactory<UnsignedByteType> factory = new ArrayImgFactory<UnsignedByteType>(new UnsignedByteType());
    SCIFIOImgPlus<UnsignedByteType> img = imgOpener.openImgs(r, factory, null).get(0);
    c.dispose();
    return img;
  }

  public static byte[] imageToJPEGBytes(SCIFIOImgPlus<UnsignedByteType> img) throws FormatException, IOException {
    final ImgSaver imgSaver = new ImgSaver();
    final Context c = imgSaver.getContext();
    final SCIFIO scifio = new SCIFIO(c);
    final JPEGFormat format = scifio.format().getFormatFromClass(JPEGFormat.class);
    final Writer w = format.createWriter();
    final BytesLocation saveLocation = new BytesLocation(10);
    w.setMetadata(img.getMetadata());
    w.setDest(saveLocation);
    imgSaver.saveImg(w, img);
    byte[] imageInByte = saveLocation.getByteBank().toByteArray();
    c.dispose();
    return imageInByte;
  }

}