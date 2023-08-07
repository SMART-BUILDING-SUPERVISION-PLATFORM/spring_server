package snust.sbsp.project.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewNoteReq {

	private Long projectId;

	private String noteType;

	private String content;
}
