package pro.belbix.tim.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "values_map")
@Cacheable(false)
@Getter
@Setter
@ToString
public class Values {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDateTime changed;
    private String type;
    private String name;
    private String value;
}
