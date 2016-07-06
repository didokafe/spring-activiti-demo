package demo.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by daniel on 06.07.16.
 */
@Entity
public class Photo {

    @Id
    @GeneratedValue
    private Long id;

    private String userId;

    private boolean processed;

    public boolean isProcessed() {
        return processed;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    // jpa
    Photo() {
    }

    public Photo(String userId) {
        this.userId = userId;
    }

    public Photo(String userId, boolean processed) {
        this.userId = userId;
        this.processed = processed;
    }
}
