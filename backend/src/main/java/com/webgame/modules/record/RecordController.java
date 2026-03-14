package com.webgame.modules.record;

import com.webgame.common.ApiResponse;
import com.webgame.common.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/me")
    public ApiResponse<?> myRecords(@RequestParam(required = false) String gameCode) {
        return ApiResponse.success(recordService.listUserRecords(UserContext.getCurrentUserId(), gameCode));
    }

    @GetMapping
    public ApiResponse<?> gameRecords(@RequestParam String gameCode) {
        return ApiResponse.success(recordService.listGameRecords(gameCode));
    }
}
