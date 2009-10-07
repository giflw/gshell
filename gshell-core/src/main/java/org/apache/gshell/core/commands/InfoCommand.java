/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.gshell.core.commands;

import jline.Terminal;
import jline.WindowsTerminal;
import org.apache.gshell.Branding;
import org.apache.gshell.ansi.Ansi;
import org.apache.gshell.cli.Argument;
import org.apache.gshell.cli.Option;
import org.apache.gshell.command.Command;
import org.apache.gshell.command.CommandActionSupport;
import org.apache.gshell.command.CommandContext;
import static org.apache.gshell.core.commands.InfoCommand.Section.*;
import org.apache.gshell.io.IO;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

//
// From Apache Felix Karaf
//

/**
 * Display shell information.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 *
 * @since 2.0
 */
@Command
public class InfoCommand
    extends CommandActionSupport
{
    private static final NumberFormat FMTI = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.ENGLISH));

    private static final NumberFormat FMTD = new DecimalFormat("###,##0.000", new DecimalFormatSymbols(Locale.ENGLISH));

    public static enum Section
    {
        SHELL,
        TERMINAL,
        JVM,
        THREADS,
        MEMORY,
        CLASSES,
        OS,
    }

    @Argument(multiValued=true)
    private List<Section> sections;

    @Option(name="-a", aliases={ "--all" })
    private boolean all;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        Branding branding = context.getShell().getBranding();
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();

        if (all) {
            sections = Arrays.asList(Section.values());
        }
        
        if (sections == null) {
            sections = Arrays.asList(SHELL);
        }
        
        //
        // TODO: i18n all this
        //

        for (Section section : sections) {
            switch (section) {
                case SHELL:
                    io.info("Shell");
                    printValue(io, "Display Name", branding.getDisplayName());
                    printValue(io, "Program Name", branding.getProgramName());
                    printValue(io, "Version", branding.getVersion());
                    printValue(io, "Home Dir", branding.getShellHomeDir());
                    printValue(io, "Context Dir", branding.getShellContextDir());
                    printValue(io, "User Home Dir", branding.getUserHomeDir());
                    printValue(io, "User Context Dir", branding.getUserContextDir());
                    printValue(io, "Script Extension", branding.getScriptExtension());
                    printValue(io, "ANSI", Ansi.isEnabled());
                    break;

                case TERMINAL:
                    io.out.println("Terminal");
                    Terminal term = io.getTerminal();
                    printValue(io, "Type", term.getClass().getName());
                    printValue(io, "Supported", term.isSupported());
                    printValue(io, "Height", term.getHeight());
                    printValue(io, "Width", term.getWidth());
                    printValue(io, "ANSI", term.isAnsiSupported());
                    printValue(io, "Echo", term.isEchoEnabled());
                    if (term instanceof WindowsTerminal) {
                        printValue(io, "Direct Console", ((WindowsTerminal)term).getDirectConsole());
                    }
                    break;

                case JVM:
                    io.out.println("JVM");
                    printValue(io, "Java Virtual Machine", runtime.getVmName() + " version " + runtime.getVmVersion());
                    printValue(io, "Vendor", runtime.getVmVendor());
                    printValue(io, "Uptime", printDuration(runtime.getUptime()));
                    try {
                        printValue(io, "Process CPU time", printDuration(getSunOsValueAsLong(os, "getProcessCpuTime") / 1000000));
                    }
                    catch (Throwable t) {}
                    printValue(io, "Total compile time", printDuration(ManagementFactory.getCompilationMXBean().getTotalCompilationTime()));
                    break;

                case THREADS:
                    io.out.println("Threads");
                    printValue(io, "Live threads", Integer.toString(threads.getThreadCount()));
                    printValue(io, "Daemon threads", Integer.toString(threads.getDaemonThreadCount()));
                    printValue(io, "Peak", Integer.toString(threads.getPeakThreadCount()));
                    printValue(io, "Total started", Long.toString(threads.getTotalStartedThreadCount()));

                    io.out.println("Memory");
                    printValue(io, "Current heap size", printSizeInKb(mem.getHeapMemoryUsage().getUsed()));
                    printValue(io, "Maximum heap size", printSizeInKb(mem.getHeapMemoryUsage().getMax()));
                    printValue(io, "Committed heap size", printSizeInKb(mem.getHeapMemoryUsage().getCommitted()));
                    printValue(io, "Pending objects", Integer.toString(mem.getObjectPendingFinalizationCount()));
                    for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                        String val = "Name = '" + gc.getName() + "', Collections = " + gc.getCollectionCount() + ", Time = " + printDuration(gc.getCollectionTime());
                        printValue(io, "Garbage collector", val);
                    }
                    break;

                case CLASSES:
                    io.out.println("Classes");
                    printValue(io, "Current classes loaded", printLong(cl.getLoadedClassCount()));
                    printValue(io, "Total classes loaded", printLong(cl.getTotalLoadedClassCount()));
                    printValue(io, "Total classes unloaded", printLong(cl.getUnloadedClassCount()));
                    break;

                case OS:
                    io.out.println("Operating system");
                    printValue(io, "Name", os.getName() + " version " + os.getVersion());
                    printValue(io, "Architecture", os.getArch());
                    printValue(io, "Processors", Integer.toString(os.getAvailableProcessors()));
                    try {
                        printValue(io, "Total physical memory", printSizeInKb(getSunOsValueAsLong(os, "getTotalPhysicalMemorySize")));
                        printValue(io, "Free physical memory", printSizeInKb(getSunOsValueAsLong(os, "getFreePhysicalMemorySize")));
                        printValue(io, "Committed virtual memory", printSizeInKb(getSunOsValueAsLong(os, "getCommittedVirtualMemorySize")));
                        printValue(io, "Total swap space", printSizeInKb(getSunOsValueAsLong(os, "getTotalSwapSpaceSize")));
                        printValue(io, "Free swap space", printSizeInKb(getSunOsValueAsLong(os, "getFreeSwapSpaceSize")));
                    }
                    catch (Throwable t) {}
                    break;
            }
        }
        
        return Result.SUCCESS;
    }

    private long getSunOsValueAsLong(OperatingSystemMXBean os, String name) throws Exception {
        Method mth = os.getClass().getMethod(name);
        return (Long) mth.invoke(os);
    }

    private String printLong(long i) {
        return FMTI.format(i);
    }

    //
    // TODO: i18n all this
    //
    
    private String printSizeInKb(double size) {
        return FMTI.format((long) (size / 1024)) + " kbytes";
    }

    private String printDuration(double uptime) {
        uptime /= 1000;
        if (uptime < 60) {
            return FMTD.format(uptime) + " seconds";
        }
        uptime /= 60;
        if (uptime < 60) {
            long minutes = (long) uptime;
            String s = FMTI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            return s;
        }
        uptime /= 60;
        if (uptime < 24) {
            long hours = (long) uptime;
            long minutes = (long) ((uptime - hours) * 60);
            String s = FMTI.format(hours) + (hours > 1 ? " hours" : " hour");
            if (minutes != 0) {
                s += " " + FMTI.format(minutes) + (minutes > 1 ? " minutes" : "minute");
            }
            return s;
        }
        uptime /= 24;
        long days = (long) uptime;
        long hours = (long) ((uptime - days) * 60);
        String s = FMTI.format(days) + (days > 1 ? " days" : " day");
        if (hours != 0) {
            s += " " + FMTI.format(hours) + (hours > 1 ? " hours" : "hour");
        }
        return s;
    }

    private void printValue(final IO io, final String name, final Object value) {
        io.out.format("  @|bold %s| = %s", name, value).println();
    }
}