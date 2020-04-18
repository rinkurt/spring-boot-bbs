package com.herokuapp.ddmura.cache;

import com.herokuapp.ddmura.dto.HotTagDTO;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class HotTagCache {
    private List<HotTagDTO> hotTagDTOList;
}
