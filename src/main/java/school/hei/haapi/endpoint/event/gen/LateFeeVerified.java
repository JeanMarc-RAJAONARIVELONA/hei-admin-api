package school.hei.haapi.endpoint.event.gen;

import static school.hei.haapi.endpoint.rest.model.Fee.TypeEnum;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import school.hei.haapi.model.User;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Data
public class LateFeeVerified implements Serializable {
  private static final long serialVersionUID = 1L;

  @JsonProperty("concerned_student")
  private User student;

  @JsonProperty("type")
  private TypeEnum type;

  @JsonProperty("remaining_amount")
  private int remainingAmount;

  @JsonProperty("due_datetime")
  private Instant dueDatetime;

  @JsonProperty("comment")
  private String comment;
}
