package pdl.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.io.File;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();

  public ImageDao() {
    try {
      final ClassPathResource imgFile = new ClassPathResource("images/");
      File file = imgFile.getFile();
      File[] listeFile = file.listFiles();
      for(File f : listeFile){
        String path = "images/"+f.getName();
        final ClassPathResource img = new ClassPathResource(path);
        byte[] fileData;
        try{
          fileData = Files.readAllBytes(img.getFile().toPath());
          Image image = new Image(f.getName(), fileData);
          images.put(image.getId(), image);
        }
        catch(final IOException e){
          e.printStackTrace();
        }
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  @Override
  public Optional<Image> retrieve(final long id) {
    return Optional.ofNullable(images.get(id)); //Important to use ofNullable
  }

  @Override
  public List<Image> retrieveAll() {
    final ArrayList<Image> imageList = new ArrayList<>();

    for(Map.Entry img : images.entrySet())
    {
      imageList.add((Image) img.getValue());
    }

    return imageList;
  }

  @Override
  public void create(final Image img) {
    if(images.put(img.getId(), img) != null)
    {
      System.err.println("Error : Existing key !");
    }
  }

  @Override
  public void update(final Image img, final String[] params) {
    //To ignore
  }

  @Override
  public void delete(final Image img) {
    if(images.remove(img.getId()) == null)
    {
      System.err.println("Error : Unexisting key to delete !");
    }
  }
}
