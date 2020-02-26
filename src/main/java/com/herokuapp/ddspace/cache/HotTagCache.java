package com.herokuapp.ddspace.cache;

import com.herokuapp.ddspace.dto.HotTagDTO;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Data
@Component
public class HotTagCache {
    private List<HotTagDTO> hotTagDTOList;
}
