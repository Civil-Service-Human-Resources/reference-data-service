package uk.gov.cshr.rds.controller;

import static java.lang.Math.toIntExact;
import java.nio.charset.Charset;
import org.assertj.core.api.Assertions;
import static org.hamcrest.Matchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.gov.cshr.rds.RdsApplication;
import uk.gov.cshr.rds.model.Department;
import uk.gov.cshr.rds.repository.DepartmentRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = RdsApplication.class)
@ContextConfiguration
@WebAppConfiguration
public class DepartmentControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DepartmentRepository departmentRepository;

    private MockMvc mvc;

    final private MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));


    private Department requestBodyDepartment = Department.builder()
            .name("department name")
            .build();



    private String requestBody = "{" +
            "\"name\":\"" + requestBodyDepartment.getName() + "\"" +
            "}";

    private Department department1 = Department.builder()
            .id(1L)
            .name("testTile1 dept")
            .build();

    private Department department2 = Department.builder()
            .id(2L)
            .name("testTitle2")
            .build();

    @BeforeMethod
    void setup() {

        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        this.departmentRepository.deleteAll();

        Department savedDepartment1 = this.departmentRepository.save(department1);
        department1.setId(savedDepartment1.getId());

        Department savedDepartment2 = this.departmentRepository.save(department2);
        department2.setId(savedDepartment2.getId());

    }

    @Test
    public void testFindAll() throws Exception {
        // Given
        String path = "/department";

        // When
        ResultActions sendRequest = mvc.perform(get(path));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].id", is(toIntExact(this.department1.getId()))))
                .andExpect(jsonPath("$.content[0].name", is(this.department1.getName())));

    }

    @Test
    public void testFindById() throws Exception {

        // Given
        String path = "/department/" + department1.getId();

        // When
        ResultActions sendRequest = mvc.perform(get(path));

		MvcResult mvcResult = sendRequest.andReturn();

		System.out.println(mvcResult.getResponse().getContentAsString());

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(toIntExact(this.department1.getId()))))
                .andExpect(jsonPath("$.name", is(this.department1.getName())));
    }

    @Test
    public void testFindByIdNotFound() throws Exception {
        // Given
        String path = "/department/-1";

        // When
        ResultActions sendRequest = mvc.perform(get(path));

        // Then
        sendRequest.andExpect(status().isNotFound());

    }

    @Test
    public void testCreate() throws Exception {
        // Given
        String path = "/department";

        // When
        ResultActions sendRequest = mvc.perform(post(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

        MvcResult sendRequestResult = sendRequest.andReturn();

        String returnedLocation = sendRequestResult.getResponse().getRedirectedUrl();

        // Then
        sendRequest
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));

        Long createdDepartmentId = getResourceIdFromUrl(returnedLocation);

        Department storedDepartment = departmentRepository.findOne(createdDepartmentId);

        Assertions.assertThat(storedDepartment).isEqualToIgnoringGivenFields(requestBodyDepartment, "id");
    }


    @Test
    public void testUpdate() throws Exception {
        // Given
        String path = "/department/" + department1.getId();

        // When
        ResultActions sendRequest = mvc.perform(put(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(toIntExact(this.department1.getId()))))
                .andExpect(jsonPath("$.name", is("department name")));
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        // Given
        String path = "/department/-1";

        // When
        ResultActions sendRequest = mvc.perform(put(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

        // Then
        sendRequest.andExpect(status().isNotFound());

    }

    private long getResourceIdFromUrl(String locationUrl) {
        String[] parts = locationUrl.split("/");
        return Long.valueOf(parts[parts.length - 1]);
    }

    @Test
    public void testDelete() throws Exception {
        // Given
        String path = "/department/" + department1.getId();

        // When
        ResultActions sendRequest = mvc.perform(delete(path));

        Iterable<Department> vacancies = departmentRepository.findAll();

        // Then
        sendRequest.andExpect(status().isNoContent());
        Assertions.assertThat(vacancies).hasSize(1);
        Assertions.assertThat(vacancies.iterator().next()).isEqualToComparingFieldByField(department2);

    }

    @Test
    public void testFindAllPaginated() throws Exception {

        // Given
        String path = "/department/?page=0&size=1";

        // When
        ResultActions sendRequest = mvc.perform(get(path));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(jsonPath("$.numberOfElements", is(1)));

    }

}