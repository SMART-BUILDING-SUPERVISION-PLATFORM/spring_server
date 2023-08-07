package snust.sbsp.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.interceptor.Interceptor;
import snust.sbsp.common.res.Response;
import snust.sbsp.project.dto.req.NewNoteReq;
import snust.sbsp.project.dto.req.ReplyNoteReq;
import snust.sbsp.project.dto.res.base.NoteDto;
import snust.sbsp.project.service.NoteService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/note")
public class NoteController {

	private final NoteService noteService;

	@PostMapping
	public ResponseEntity<?> addNewNote(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@RequestBody NewNoteReq newNoteReq
	) {
		noteService.addNote(currentCrewId, newNoteReq);

		return Response.ok(HttpStatus.CREATED);
	}

	@PutMapping
	public ResponseEntity<?> updateNote(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@RequestBody ReplyNoteReq replyNoteReq
	) {
		noteService.updateNote(currentCrewId, replyNoteReq);

		return Response.ok(HttpStatus.OK);
	}

	@GetMapping("/user/{id}")
	public ResponseEntity<List<NoteDto>> getNoteList(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId,
		@RequestParam("type") String noteType,
		@RequestParam("new") Boolean isNew
	) {

		List<NoteDto> noteList = noteService.readNote(currentCrewId, projectId, noteType, isNew);

		return Response.ok(HttpStatus.OK, noteList);
	}

	@GetMapping("/admin/{id}")
	public ResponseEntity<List<NoteDto>> getNoteListForSa(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId,
		@RequestParam("type") String noteType,
		@RequestParam("new") Boolean isNew
	) {

		List<NoteDto> noteList = noteService.readNoteForSa(currentCrewId, projectId, noteType, isNew);

		return Response.ok(HttpStatus.OK, noteList);
	}

}
