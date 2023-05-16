package snust.sbsp.crew.domain;

import lombok.*;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.CrewRes;
import snust.sbsp.project.domain.Participant;
import snust.sbsp.project.dto.res.base.ProjectDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@Entity(name = "crew")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Crew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "role")
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Column(name = "is_pending")
    private boolean isPending;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "crew", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    private List<Participant> participantList = new ArrayList<>();

    public void togglePending() {
        this.isPending = !isPending;
    }

    public List<ProjectDto> getProjectDtoList() {
        return participantList.stream().map(p -> new ProjectDto(p.getProject())).collect(Collectors.toList());
    }
}
