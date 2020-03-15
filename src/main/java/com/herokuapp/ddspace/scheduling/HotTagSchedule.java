package com.herokuapp.ddspace.scheduling;

import com.herokuapp.ddspace.cache.HotTagCache;
import com.herokuapp.ddspace.dto.HotTagDTO;
import com.herokuapp.ddspace.mapper.QuestionMapper;
import com.herokuapp.ddspace.model.Question;
import com.herokuapp.ddspace.model.QuestionExample;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class HotTagSchedule {

    private static final int queueSize = 5;
    private static final Comparator<HotTagDTO> comparator = Comparator.comparingInt(HotTagDTO::getPriority);

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private HotTagCache hotTagCache;


    public int calcPriority(Question question) {
        return question.getViewCount() + 2 * question.getLikeCount() + 3 * question.getCommentCount() + 20;
    }


    @Scheduled(fixedRate = 18000000) // 5 hours
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
