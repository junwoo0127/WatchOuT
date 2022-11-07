package com.ssafy.watchout.api.domain.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MMSMessageRequestDto {
	List<String> receivers;
	String message;
	List<MultipartFile> files;
}
