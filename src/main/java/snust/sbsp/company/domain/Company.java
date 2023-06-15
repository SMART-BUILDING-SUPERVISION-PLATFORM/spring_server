package snust.sbsp.company.domain;

import lombok.*;
import snust.sbsp.company.dto.res.base.CompanyDto;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.project.domain.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Entity(name = "company")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "company", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Crew> crewList = new ArrayList<>();

    @OneToMany(mappedBy = "company", orphanRemoval = true, cascade = {CascadeType.PERSIST})
    private List<Project> projectList = new ArrayList<>();

    public CompanyDto toDto() {
        return new CompanyDto(this);
    }
}
