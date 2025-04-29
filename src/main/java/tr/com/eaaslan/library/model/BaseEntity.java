package tr.com.eaaslan.library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(updatable = false, nullable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(nullable = false)
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }
}

//1. Robustness and Resilience
//By having two mechanisms, you ensure that audit timestamps are always set, even if one mechanism fails. This is particularly important for audit fields which often have constraints like nullable = false.
//2. Testing Simplicity
//When writing unit tests for your entities, you don't have to set up the entire Spring auditing infrastructure - the JPA lifecycle methods will ensure your timestamps are correctly set.
//        3. Consistent Behavior
//Having both mechanisms ensures consistent behavior regardless of how the entity is persisted (through repositories, EntityManager, or other means).
//Is There a Performance Impact?
//The performance impact of having both mechanisms is negligible:
//
//The JPA lifecycle methods only execute if a value hasn't already been set
//Both operations involve simply setting a timestamp, which is a very lightweight operation
//In most cases, Spring's auditing will set the values first, making the lifecycle methods essentially no-ops