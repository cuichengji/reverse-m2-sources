package com.reverse.project.task.sources.cmd;

import cn.hutool.core.collection.CollectionUtil;
import com.reverse.project.base.task.AbstractTaskCommand;
import com.reverse.project.task.sources.context.ReverseSourceContext;
import com.reverse.project.task.sources.dto.SourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 将pom.xml及sources进行收集
 * 以便下步进行pom.xml分析及源码完整性匹配
 * @author guoguoqiang
 * @since 2020年07月07日
 */
@Slf4j
@Component
public class CollectSourcesCmd extends AbstractTaskCommand<ReverseSourceContext> {

    @Override
    public boolean exec(ReverseSourceContext context) throws Exception {
        List<SourceDTO> sources = context.getMiddle().getSourceList();
        if (CollectionUtil.isEmpty(sources)) {
            log.error("需要逆向的文件列表为空");
            return true;
        }

        return false;
    }

}
