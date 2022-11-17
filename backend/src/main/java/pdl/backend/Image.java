package pdl.backend;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class Image {
  private static Long count = Long.valueOf(0);
  private Long id;
  private String name;
  private byte[] data;

  public Image(final String name, final byte[] data) {
    id = count++;
    this.name = name;
    this.data = data;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }

  public String getSize() throws IOException {

    InputStream in = new ByteArrayInputStream(data);

    BufferedImage buf = ImageIO.read(in);
    ColorModel model = buf.getColorModel();
    int height = buf.getHeight();
    int width = buf.getWidth();
    String size = height + "*" + width + "*" + model.getNumComponents();

    return size;
  }

  public String getType(){
    String contentType="";
    try{
      contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(this.data));
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return contentType;
    
  }
}
