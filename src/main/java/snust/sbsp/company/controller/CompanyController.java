package snust.sbsp.company.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import snust.sbsp.common.res.Response;
import snust.sbsp.company.dto.res.CompanyRes;
import snust.sbsp.company.service.CompanyService;

import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<?> validateCompany(@PathParam("name") String name) {
        List<CompanyRes> companyList = companyService.findByName(name);

        return Response.ok(HttpStatus.OK, companyList);
    }

}
