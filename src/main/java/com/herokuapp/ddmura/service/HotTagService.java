package com.herokuapp.ddmura.service;

import com.herokuapp.ddmura.cache.HotTagCache;
import com.herokuapp.ddmura.dto.HotTagDTO;
import com.herokuapp.ddmura.mapper.QuestionMapper;
import com.herokuapp.ddmura.model.Question;
import com.herokuapp.ddmura.model.QuestionExample;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class HotTagService {

    private static final int queueSize = 5;
    private static final Comparator<HotTagDTO> comparator = Comparator.comparingInt(HotTagDTO::getPriority);

    // Autowired
    private QuestionMapper questionMapper;
    private HotTagCache hotTagCache;


    public int calcPriority(Question question) {
        return question.getViewCount() + 2 * question.getLikeCount() + 3 * question.getCommentCount() + 20;
    }

    public void getHotTags() {
        List<Question> questions = questionMapper.selectByExample(new QuestionExample());
        HashMap<String, HotTagDTO> tagMap = new HashMap<>();
        for (Question question : questions) {
            for (String tag : question.getTag().split(",")) {
                tag = tag.trim().toLowerCase();
                HotTagDTO hotTagDTO = tagMap.get(tag);
                if (hotTagDTO == null) {
                    hotTagDTO = new HotTagDTO();
                    hotTagDTO.setTag(tag);
                    hotTagDTO.incQuestion();
                    hotTagDTO.setCommentCount(question.getCommentCount());
                    hotTagDTO.setPriority(calcPriority(question));
                    tagMap.put(tag, hotTagDTO);
                } else {
                    hotTagDTO.incQuestion();
                    hotTagDTO.incComment(question.getCommentCount());
                    hotTagDTO.incPriority(calcPriority(question));
                    tagMap.put(tag, hotTagDTO);
                }
            }
        }
        PriorityQueue<HotTagDTO> priorityQueue = new PriorityQueue<>(queueSize, comparator);
        tagMap.forEach(
                (tag, hotTagDTO) -> {
                    if (priorityQueue.size() < queueSize) {
                        priorityQueue.add(hotTagDTO);
                    } else {
                        HotTagDTO peek = priorityQueue.peek();
                        if (hotTagDTO.getPriority() > peek.getPriority()) {
                            priorityQueue.poll();
                            priorityQueue.add(hotTagDTO);
                        }
                    }

                }
        );
        List<HotTagDTO> hotTagDTOList = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            hotTagDTOList.add(0, priorityQueue.peek());
            priorityQueue.poll();
        }

        hotTagCache.setHotTagDTOList(hotTagDTOList);

    }
}
