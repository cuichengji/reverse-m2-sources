/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reverse.project.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Properties;

/**
 * 命令行工具类
 * 参数列表
* @change 2024/06/13 sai 追加a,b参数
 */
public class CliUtil {

    public static Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("h", "help", false, "Print help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("m", "m2", true,
                "m2 directory, eg: /.m2");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("s", "scan", true,
        "Directory to be scanned, eg: d:/.m2/org/springframework.Note:scan directory must be sub directory of m2 directory.");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("t", "tmp", true,
        "The tmp directory is used to store the unzipped sources.jar, eg: /.m2/tmp");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("o", "output", true,
        "The output directory is store reversed source, eg: /.m2/output");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("a", "skip pom Analysis", true,
                "skip pom Analysis, eg: true");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("b", "before version", true,
                "The output directory before version after ArtifactId, eg: true");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    public static CommandLine parseCmdLine(final String appName, String[] args, Options options,
        CommandLineParser parser) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption('h')) {
                hf.printHelp(appName, options, true);
                return null;
            }
        } catch (ParseException e) {
            hf.printHelp(appName, options, true);
        }

        return commandLine;
    }

    public static void printCommandLineHelp(final String appName, final Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        hf.printHelp(appName, options, true);
    }

    public static Properties commandLine2Properties(final CommandLine commandLine) {
        Properties properties = new Properties();
        Option[] opts = commandLine.getOptions();

        if (opts != null) {
            for (Option opt : opts) {
                String name = opt.getLongOpt();
                String value = commandLine.getOptionValue(name);
                if (value != null) {
                    properties.setProperty(name, value);
                }
            }
        }

        return properties;
    }

}
