package com.controller;

import com.json.JsonResponses;
import com.unisystems.UniStructureApplication;
import com.unisystems.utils.Utils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UniStructureApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskFeature {

    MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    Utils utils;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void getAllTasks() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/task/getAll")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskResponses").isArray())
                .andExpect(jsonPath("$.taskResponses[0].taskId").exists());
    }

    @Test
    public void getTaskById() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/task/findById/{taskId}","1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        assertEquals(result.getResponse().getContentAsString(), JsonResponses.jsonTaskById);
    }

    @Test
    public void getTaskByDifficulty() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/task/findByDifficulty/{difficulty}","hard")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.taskResponses", hasSize(1)))
                .andExpect(jsonPath("$.taskResponses").isArray())
                .andExpect(jsonPath("$.taskResponses[0].taskId").exists());
    }

    @Test
    public void getAndFindByAssignedEmployees() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/task/findByAssignedEmployees/{assignedEmployees}","1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.taskResponses", hasSize(1)))
                .andExpect(jsonPath("$.taskResponses").isArray())
                .andExpect(jsonPath("$.taskResponses[0].taskId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskResponses[0].taskStatus").value("NEW"));
    }

    @Test
    public void getAndFindByAssignedEmployeesAndDifficulty() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/task/findByAssignedEmployeesAndDifficulty/{difficulty}/{assignedEmployees}","medium","0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.taskResponses", hasSize(1)))
                .andExpect(jsonPath("$.taskResponses").isArray())
                .andExpect(jsonPath("$.taskResponses[0].taskId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskResponses[0].taskStatus").value("NEW"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskResponses[*].taskId").isNotEmpty());
    }

    @Test
    public void createTask() throws Exception {
        this.mockMvc.perform( MockMvcRequestBuilders
                .post("/task/postTask")
                .header("title","Title").header("desc","Desc")
                .header("estimationA","estimationA").header("estimationB","estimationB")
                .header("estimationC","estimationC").header("status","new")
                .header("employees","2,3").header("updates","test,test")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskId").doesNotExist());

        this.mockMvc.perform( MockMvcRequestBuilders
                .post("/task/postTask")
                .header("title","Title").header("desc","Desc")
                .header("estimationA",2).header("estimationB",3)
                .header("estimationC",5).header("status","new")
                .header("updates","test,test").header("employees","2,4")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskResponses", hasSize(1)))
                .andExpect(jsonPath("$.taskResponses").isArray())
                .andExpect(jsonPath("$.taskResponses[0].taskId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskResponses[0].taskStatus").value("NEW"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskResponses[*].taskId").isNotEmpty());
    }

    @Test
    public void updateTask() throws Exception {
        this.mockMvc.perform( MockMvcRequestBuilders
                .put("/task/putTask/{taskId}", 2)
                .header("title","Title").header("desc","Desc")
                .header("estimationA",2).header("estimationB",3)
                .header("estimationC",5).header("status","new")
                .header("updates","test,test").header("employees","null")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskResponses", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskResponses[0].taskDesc").value("Desc"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskResponses[0].taskTitle").value("Title"));
    }

    @Test
    public void patchTask() throws Exception {
        this.mockMvc.perform( MockMvcRequestBuilders
                .patch("/task/patchTask/{id}", 2)
                .header("columnName","title")
                .header("data","patchedTitle")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskResponses", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskResponses[0].taskDesc").value("This is a task for making a new portal"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskResponses[0].taskTitle").value("patchedTitle"));
    }

    @Test
    public void deleteTask() throws Exception {
        this.mockMvc.perform( MockMvcRequestBuilders.delete("/task/deleteAllTasks") )
            .andExpect(status().isAccepted());
    }

    @Test
    public void deleteTaskById() throws Exception {
        MvcResult result =this.mockMvc.perform( MockMvcRequestBuilders.delete("/task/deleteTask/{taskId}","100") )
                .andDo(print())
                .andExpect(status().isNotAcceptable()).andReturn();
        assertEquals(result.getResponse().getContentAsString(), JsonResponses.jsonResponseDeleteById);
    }
}
