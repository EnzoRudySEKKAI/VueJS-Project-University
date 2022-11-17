package pdl.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Order(1)
	public void getImageListShouldReturnSuccess() throws Exception {
		mockMvc.perform(get("/images/")).andExpect(status().isOk())
		.andExpect(content().json("[{'id':0, 'name':'logo.jpg'}]"));
	}

	@Test
	@Order(2)
	public void getImageShouldReturnNotFound() throws Exception {
		mockMvc.perform(get("/images/1")).andExpect(status().isNotFound());
	}

	@Test
	@Order(3)
	public void getImageShouldReturnSuccess() throws Exception {
		mockMvc.perform(get("/images/0")).andExpect(status().isOk());
	}

	@Test
	@Order(4)
	public void deleteImageShouldReturnBadRequest() throws Exception {
		mockMvc.perform(delete("/images/xxxxX")).andExpect(status().isBadRequest()); // id is a long integer
	}

	@Test
	@Order(5)
	public void deleteImageShouldReturnNotFound() throws Exception {
		mockMvc.perform(delete("/images/1")).andExpect(status().isNotFound());
	}

	@Test
	@Order(6)
	public void deleteImageShouldReturnSuccess() throws Exception {
		mockMvc.perform(delete("/images/0")).andExpect(status().isOk());
	}

	@Test
	@Order(7)
	public void createImageShouldReturnSuccess() throws Exception {
		MockMultipartFile image = new MockMultipartFile("file", "", 
		"image/jpeg", "{\"file\": \"test.jpg\"}".getBytes());

		mockMvc.perform(
			MockMvcRequestBuilders.fileUpload("/images/").file(image)).andExpect(status().isCreated());
	}

	@Test
	@Order(8)
	public void createImageShouldReturnUnsupportedMediaType() throws Exception {
		MockMultipartFile image = new MockMultipartFile("file", "", 
		"image/png", "{\"file\": \"test.png\"}".getBytes());

		mockMvc.perform(
			MockMvcRequestBuilders.fileUpload("/images/").file(image)).andExpect(status().isUnsupportedMediaType());
	}
	
}
