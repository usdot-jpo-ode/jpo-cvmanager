package us.dot.its.jpo.ode.api.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;

@RestController
public class IntersectionController {

    @Autowired
    ProcessedMapRepository processedMapRepo;

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/intersection/list", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN')")
    public ResponseEntity<List<IntersectionReferenceData>> getIntersections(
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            IntersectionReferenceData ref = new IntersectionReferenceData();
            ref.setRsuIP("10.11.81.12");
            ref.setIntersectionID(12109);
            ref.setRoadRegulatorID("0");

            List<IntersectionReferenceData> refList = new ArrayList<>();
            refList.add(ref);

            return ResponseEntity.ok(refList);
        } else {

            return ResponseEntity.ok(processedMapRepo.getIntersectionIDs());

        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/intersection/list/location", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN')")
    public ResponseEntity<List<IntersectionReferenceData>> getIntersectionsByLocation(
            @RequestParam(name = "longitude", required = true, defaultValue = "false") Double longitude,
            @RequestParam(name = "latitude", required = true, defaultValue = "false") Double latitude,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            IntersectionReferenceData ref = new IntersectionReferenceData();
            ref.setRsuIP("10.11.81.12");
            ref.setIntersectionID(12109);
            ref.setRoadRegulatorID("0");

            List<IntersectionReferenceData> refList = new ArrayList<>();
            refList.add(ref);

            return ResponseEntity.ok(refList);
        } else {
            return ResponseEntity.ok(processedMapRepo.getIntersectionsContainingPoint(longitude, latitude));
        }
    }
}
