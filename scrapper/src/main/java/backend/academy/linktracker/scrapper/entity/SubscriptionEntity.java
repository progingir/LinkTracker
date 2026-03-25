package backend.academy.linktracker.scrapper.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionEntity {

    @EmbeddedId
    private SubscriptionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("linkId")
    @JoinColumn(name = "link_id")
    private LinkEntity link;

    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "subscription_tag",
            joinColumns = {
                @JoinColumn(name = "chat_id", referencedColumnName = "chat_id"),
                @JoinColumn(name = "link_id", referencedColumnName = "link_id")
            },
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
    private List<TagEntity> tags = new ArrayList<>();
}
