package se.sowl.devlyexternal.client.gpt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrGPTResponse {
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("changedFiles")
    private List<ChangedFileDto> changedFiles;
    
    @JsonProperty("labels")
    private List<String> labels;
    
    @JsonProperty("reviewComments")
    private List<String> reviewComments;
}