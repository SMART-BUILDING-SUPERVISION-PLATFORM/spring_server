package snust.sbsp.crew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.repository.CrewRepository;

@RequiredArgsConstructor
@Service
public class CrewService {

    private final CrewRepository crewRepository;

    public Crew readCrew(String crewEmail) {
        return crewRepository.findByEmail(crewEmail)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
    }

    public Crew readCrew(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
    }
}
