package com.example.RESTfulTest.controller;

import com.example.RESTfulTest.model.Widget;
import com.example.RESTfulTest.service.WidgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WidgetRestControllerTest {

    @MockBean
    private WidgetService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /widgets success")
    public void getExitoso() throws Exception{
        //Arrange
        Widget widget1 = new Widget(1l, "Widget Name", "Description", 1);
        Widget widget2 = new Widget(2l, "Widget 2 Name", "Description 2", 4);

        doReturn(Lists.newArrayList(widget1,widget2)).when(service).findAll();

        //Act && Assert

        mockMvc.perform(get("/rest/widgets"))
                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widgets"))

                // Validate the returned fields
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Widget Name")))
                .andExpect(jsonPath("$[0].description", is("Description")))
                .andExpect(jsonPath("$[0].version", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Widget 2 Name")))
                .andExpect(jsonPath("$[1].description", is("Description 2")))
                .andExpect(jsonPath("$[1].version", is(4)));
    }

    @Test
    @DisplayName("GET /rest/widget/1 - Found")
    void testGetWidgetByIdFound() throws Exception{
        Widget widgetToReturn = new Widget(1L,"New Widget","This is a widget",1);
        doReturn(Optional.of(widgetToReturn)).when(service).findById(any());
        //Act
        mockMvc.perform(get("/rest/widget/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))

                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Widget")))
                .andExpect(jsonPath("$.description", is("This is a widget")))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("GET /rest/widget/1 - Not Found")
    void testGetWidgetByIdNotFound() throws Exception{
        doReturn(Optional.empty()).when(service).findById(1L);
        mockMvc.perform(get("/rest/widget/{id}",1L))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("POST /rest/widget")
    void testCreateWidget() throws Exception{
        //Arrange
        Widget widgetToPost = new Widget("New Widget","This is a new widget");
        Widget widgetToReturn = new Widget(1L,"New Widget","This is a new widget",1);
        doReturn(widgetToReturn).when(service).save(any());

        //Act
        mockMvc.perform(post("/rest/widget")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(widgetToPost))
         )
                //Asserts
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION,"/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG,"\"1\""))
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.name",is("New Widget")))
                .andExpect(jsonPath("$.description",is("This is a new widget")))
                .andExpect(jsonPath("$.version",is(1)));
    }

    @Test
    @DisplayName("PUT /rest/widget/1 Found ")
    void testUpdateWidgetFound() throws Exception {
        Widget actualWidget = new Widget(1L, "Actual Widget", "This is actual widget", 1);
        doReturn(Optional.of(actualWidget)).when(service).findById(any());
        Widget newWidget = new Widget(1L, "New Widget", "This is update widget", 1);
        doReturn(newWidget).when(service).save(any());

        //Act
        mockMvc.perform(put("/rest/widget/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newWidget))
                .header("If-Match",1)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION,"/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG,"\"1\""))
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.name",is("New Widget")))
                .andExpect(jsonPath("$.description",is("This is update widget")))
                .andExpect(jsonPath("$.version",is(1)));

    }
    @Test
    @DisplayName("PUT /rest/widget/1 Not - Found ")
    void testUpdateWidgetNotFound() throws Exception {
        doReturn(Optional.empty()).when(service).findById(any());
        Widget newWidget = new Widget();
        //doReturn(newWidget).when(service).save(any());

        //Act
        mockMvc.perform(put("/rest/widget/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newWidget))
                .header("If-Match", 1)
        )
                .andExpect(status().isNotFound());
    }
        static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}