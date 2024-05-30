package com.vaidhyamegha.data_cloud.kg;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "eSearchResult", namespace = "")
public class ESearchResult {

    @XmlElement(name = "Count", namespace = "")
    private Integer count;

    @XmlElement(name = "RetMax", namespace = "")
    private Integer retMax;

    @XmlElement(name = "RetStart", namespace = "")
    private Integer retStart;

    @XmlElementWrapper(name="IdList")
    @XmlElement(name="Id")
    private List<Integer> idList;

    @Override
    public String toString() {
        return "ESearchResult{" +
                "count=" + count +
                ", retMax=" + retMax +
                ", retStart=" + retStart +
                ", idList=" + idList +
                '}';
    }
}