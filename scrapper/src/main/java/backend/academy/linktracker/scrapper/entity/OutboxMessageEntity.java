package backend.academy.linktracker.scrapper.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "outbox_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxMessageEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String payload;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private Integer attempts = 0;

    public enum OutboxStatus {
        PENDING,
        SENT,
        FAILED
    }
}
