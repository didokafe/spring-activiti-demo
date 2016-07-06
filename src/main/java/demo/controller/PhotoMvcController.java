package demo.controller;

import demo.component.PhotoService;
import demo.model.Photo;
import demo.repository.PhotoRepository;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by daniel on 06.07.16.
 */
@Controller
public class PhotoMvcController {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private TaskService taskService;


    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    String upload(MultipartHttpServletRequest request, Principal principal) {

        System.out.println("uploading for " + principal.toString());
        Optional.ofNullable(request.getMultiFileMap()).ifPresent(m -> {

            // gather all the MFs in one collection
            List<MultipartFile> multipartFiles = new ArrayList<>();
            m.values().forEach((t) -> {
                multipartFiles.addAll(t);
            });

            // convert them all into `Photo`s
            List<Photo> photos = multipartFiles.stream().map(f -> {
                try {
                    return this.photoService.createPhoto(principal.getName(), f.getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            System.out.println("started photo process w/ process instance ID: " + this.photoService.launchPhotoProcess(photos).getId());

        });
        return "redirect:/";
    }

    @RequestMapping(value = "/image/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    Resource image(@PathVariable Long id) {
        return new InputStreamResource(this.photoService.readPhoto(this.photoRepository.findOne(id)));
    }

    @RequestMapping(value = "/approve", method = RequestMethod.POST)
    String approveTask(@RequestParam String taskId, @RequestParam String approved) {
        boolean isApproved = !(approved == null || approved.trim().equals("") || approved.toLowerCase().contains("f") || approved.contains("0"));
        this.taskService.complete(taskId, Collections.singletonMap("approved", isApproved));
        return "redirect:approve";
    }

    @RequestMapping(value = "/approve", method = RequestMethod.GET)
    String setupApprovalPage(Model model) {
        List<Task> outstandingTasks = taskService.createTaskQuery()
                .taskCandidateGroup("photoReviewers")
                .list();
        if (0 < outstandingTasks.size()) {
            Task task = outstandingTasks.iterator().next();
            model.addAttribute("task", task);
            List<Photo> photos = (List<Photo>) taskService.getVariable(task.getId(), "photos");
            model.addAttribute("photos", photos);
        }
        return "approve";
    }
}
