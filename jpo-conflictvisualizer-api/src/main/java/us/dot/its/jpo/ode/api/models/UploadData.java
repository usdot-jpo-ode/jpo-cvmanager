package us.dot.its.jpo.ode.api.models;
 
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
 
@ToString
@Setter
@EqualsAndHashCode
@Getter
public class UploadData {
    public boolean test;
    public List<String> bsmList = new ArrayList<>();
    public List<String> spatList = new ArrayList<>();
    public List<String> mapList = new ArrayList<>();
    public List<String> genericUpload = new ArrayList<>();
    public Long uploadTime;
    public String ID;
}