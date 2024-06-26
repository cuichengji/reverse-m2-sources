package com.reverse.project.task.sources.cmd;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import com.reverse.project.base.task.AbstractTaskCommand;
import com.reverse.project.constants.Constants;
import com.reverse.project.constants.FileTypeEnum;
import com.reverse.project.constants.ReverseFailEnum;
import com.reverse.project.task.sources.context.ReverseSourceContext;
import com.reverse.project.task.sources.vo.ErrorSourceVO;
import com.reverse.project.task.sources.vo.ModuleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 生成项目源代码
 * 输入：middle.moduleMap
 * 输出：
 * 生成源码在outputDir目录
 * output.errorSources、output.successSources
 * @author guoguoqiang
 * @since 2020年07月09日
 * @change 2024/06/13 sai 追加b参数处理
 */
@Slf4j
@Component
public class GenerateProjectSourceCmd extends AbstractTaskCommand<ReverseSourceContext> {
    @Override
    public boolean exec(ReverseSourceContext context) throws Exception {
        final String outputDir = context.getOutputDir();
        List<ModuleVO> successSources = Lists.newArrayList();
        List<ErrorSourceVO> errorSources = context.getOutput().getErrorSources();
        Map<String, ModuleVO> moduleMap = context.getMiddle().getModuleMap();
        moduleMap.forEach((k, m) -> {
            log.info("generate source:{},version:{}", m.getArtifactId(), m.getVersion());
            try {
                StringBuilder outputDirBase = new StringBuilder(outputDir);
                if (ModuleVO.moduleIsSources(m)) {
                    outputDirBase.append(File.separator).append(Constants.FOLDER_SOURCES);
                } else {
                    outputDirBase.append(File.separator).append(Constants.FOLDER_POM);
                }
                generateSource(context, m, outputDirBase.toString());
                successSources.add(m);
            } catch (Exception e) {
                log.error("source generate failed:{}，{}", k, e.getMessage(), e);
                ErrorSourceVO errorSource = buildErrorSource(m, ReverseFailEnum.FAIL_REVERSE_SOURCE);
                errorSource.setReverseFailDescription("source generate failed:" + e.getMessage());
                errorSources.add(errorSource);
            }
        });
        context.getOutput().setSuccessSources(successSources);
        return false;
    }

    /**
     * 递归生成代码
     * @param module module
     * @param outputDir outputDir
     * @throws IOException IOException
     */
    private void generateSource(ReverseSourceContext context, ModuleVO module, String outputDir) throws IOException {
        StringBuilder sb = new StringBuilder(outputDir);
        if (context.isBeforeVersion()) {
            // 根项目下需要加上版本号
            if (module.getParent() == null) {
                sb.append(File.separator).append(module.getVersion());
            }
        }
        sb.append(File.separator).append(module.getArtifactId());
        // 根项目下需要加上版本号
        if (!context.isBeforeVersion()) {
            if (module.getParent() == null) {
                sb.append(File.separator).append(module.getVersion());
            }
        }
        File file = new File(sb.toString());
        if (file.exists()) {
            FileUtils.forceDelete(file);
            FileUtils.forceMkdir(file);
        }
        module.setModuleGenerateDir(FileUtil.getAbsolutePath(file));
        FileUtils.copyFile(new File(module.getPomPath()), new File(sb.toString() + File.separator + "pom.xml"));
        if (FileTypeEnum.FILE_TYPE_SOURCES.getCode() == module.getFileType()) {
            mkdirSourceDir(sb.toString());
            File sourceJarFolder = new File(module.getSourcesPath());
            File[] listFile = sourceJarFolder.listFiles();
            if (listFile != null && listFile.length > 0) {
                for (File f : listFile) {
                    if (f.isFile()) {
                        FileUtils.copyFile(f, new File(sb.toString() + File.separator
                            + Constants.RESOURCES_DIRECTORY + File.separator + f.getName()));
                    }
                    if (f.isDirectory()) {
                        if (isJavaFolder(f)) {
                            FileUtils.copyDirectory(f, new File(sb.toString() + File.separator + Constants.SRC_DIRECTORY + File.separator + f.getName()));
                        } else {
                            FileUtils.copyDirectory(f, new File(sb.toString() + File.separator + Constants.RESOURCES_DIRECTORY + File.separator + f.getName()));
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(module.getModules())) {
            return;
        }
        for (ModuleVO m: module.getModules()) {
            generateSource(context, m, sb.toString());
        }
    }

    private ErrorSourceVO buildErrorSource(ModuleVO module, ReverseFailEnum failEnum) {
        ErrorSourceVO errorSource = new ErrorSourceVO();
        BeanUtils.copyProperties(module, errorSource);
        errorSource.setFailEnum(failEnum);
        return errorSource;
    }

    private void mkdirSourceDir(String baseDir) throws IOException {
        File srcDirectory = new File(baseDir + File.separator + Constants.SRC_DIRECTORY);
        FileUtils.forceMkdir(srcDirectory);
        File resourcesDirectory = new File(baseDir + File.separator + Constants.RESOURCES_DIRECTORY);
        FileUtils.forceMkdir(resourcesDirectory);
    }

    /**
     * 判断该目录是否含java文件
     * @param target sources.jar解压后子文件夹
     * @return true该文件夹含java文件 false不含
     */
    private boolean isJavaFolder(File target) {
        if (!target.exists() || target.isFile()) {
            return false;
        }
        File[] listFile = target.listFiles();
        if (listFile == null || listFile.length == 0) {
            return false;
        }
        boolean match = Arrays.stream(listFile).anyMatch(f -> f.isFile() && f.getName().toLowerCase().endsWith(Constants.JAVA_FIX));
        if (match) {
            return true;
        }
        for (File item: listFile) {
            if (item.isDirectory() && isJavaFolder(item)) {
               return true;
            }
        }
        return false;
    }

}
