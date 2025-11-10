package com.ct.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.api.PageResult;
import com.ct.wms.common.api.Result;
import com.ct.wms.entity.Message;
import com.ct.wms.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息管理Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "消息查询、标记已读接口")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/my")
    @Operation(summary = "查询我的消息列表", description = "分页查询当前用户的消息")
    public Result<PageResult<Message>> listMyMessages(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "是否已读: 0-未读 1-已读") @RequestParam(required = false) Integer isRead) {

        log.info("查询我的消息: pageNum={}, pageSize={}, isRead={}", pageNum, pageSize, isRead);

        Page<Message> page = messageService.listMyMessages(pageNum, pageSize, isRead);

        return Result.success(PageResult.of(page));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数量", description = "返回当前用户的未读消息数量")
    public Result<Long> getUnreadCount() {
        log.info("获取未读消息数量");
        Long count = messageService.getUnreadCount();
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记消息为已读", description = "标记单条消息为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        log.info("标记消息为已读: id={}", id);
        messageService.markAsRead(id);
        return Result.success(null, "标记成功");
    }

    @PutMapping("/read-batch")
    @Operation(summary = "批量标记消息为已读", description = "批量标记多条消息为已读")
    public Result<Void> batchMarkAsRead(
            @Parameter(description = "消息ID列表") @RequestBody List<Long> ids) {
        log.info("批量标记消息为已读: ids={}", ids);
        messageService.batchMarkAsRead(ids);
        return Result.success(null, "批量标记成功");
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记所有消息为已读", description = "标记当前用户的所有未读消息为已读")
    public Result<Void> markAllAsRead() {
        log.info("标记所有消息为已读");
        messageService.markAllAsRead();
        return Result.success(null, "标记成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "删除单条消息（软删除）")
    public Result<Void> deleteMessage(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        log.info("删除消息: id={}", id);
        messageService.deleteMessage(id);
        return Result.success(null, "删除成功");
    }
}
