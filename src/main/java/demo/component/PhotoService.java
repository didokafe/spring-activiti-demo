package demo.component;

import demo.model.Photo;
import demo.repository.PhotoRepository;
import doge.photo.PhotoManipulator;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.h2.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Collections;
import java.util.List;


/**
 * Created by daniel on 06.07.16.
 */
@Service
@Transactional
public class PhotoService {

    @Autowired
    private TaskService taskService;

    @Value("file://#{ systemProperties['user.home'] }/Desktop/uploads")
    private Resource uploadDirectory;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private PhotoRepository photoRepository;

    @PostConstruct
    public void beforeService() throws Exception {
        File uploadDir = this.uploadDirectory.getFile();
        Assert.isTrue(uploadDir.exists() || uploadDir.mkdirs(), "the " + uploadDir.getAbsolutePath() + " folder must exist!");
    }

    protected void writePhoto(Photo photo, InputStream inputStream) {
        try {
            try (InputStream input = inputStream;
                 FileOutputStream output = new FileOutputStream(new File(this.uploadDirectory.getFile(), Long.toString(photo.getId())))) {
                IOUtils.copy(input, output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream readPhoto(Photo photo) {
        try {
            return new FileInputStream(new File(this.uploadDirectory.getFile(), Long.toString(photo.getId())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Photo createPhoto(String userId, InputStream bytesForImage) {
        Photo photo = this.photoRepository.save(new Photo((userId), false));
        writePhoto(photo, bytesForImage);
        return photo;
    }

    public ProcessInstance launchPhotoProcess(List<Photo> photos) {
        return runtimeService.startProcessInstanceByKey("photoProcess", Collections.singletonMap("photos", photos));
    }

    public void dogifyPhoto(Photo photo) {
        try {
            InputStream inputStream = this.photoManipulator.manipulate(() -> this.readPhoto(photo)).getInputStream();
            writePhoto(photo, inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private PhotoManipulator photoManipulator;
}
