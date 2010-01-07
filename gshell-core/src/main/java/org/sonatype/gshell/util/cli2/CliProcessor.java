/*
 * Copyright (C) 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonatype.gshell.util.cli2;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.sonatype.gossip.Log;
import org.sonatype.gshell.util.IllegalAnnotationError;
import org.sonatype.gshell.util.cli2.handler.Handler;
import org.sonatype.gshell.util.cli2.handler.Handlers;
import org.sonatype.gshell.util.i18n.MessageSource;
import org.sonatype.gshell.util.setter.SetterFactory;
import org.sonatype.gshell.util.yarn.Yarn;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Processes an object for cli annotations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.3
 */
public class CliProcessor
{
    private static final Logger log = Log.getLogger(CliProcessor.class);

    private final List<OptionDescriptor> optionDescriptors = new ArrayList<OptionDescriptor>();

    private final List<ArgumentDescriptor> argumentDescriptors = new ArrayList<ArgumentDescriptor>();

    private boolean stopAtNonOption;

    private MessageSource messages;

    public CliProcessor() {
    }

    public boolean isStopAtNonOption() {
        return stopAtNonOption;
    }

    public void setStopAtNonOption(boolean flag) {
        this.stopAtNonOption = flag;
    }

    public MessageSource getMessages() {
        return messages;
    }

    public void setMessages(final MessageSource messages) {
        this.messages = messages;
    }

    public List<OptionDescriptor> getOptionDescriptors() {
        return optionDescriptors;
    }

    public List<ArgumentDescriptor> getArgumentDescriptors() {
        return argumentDescriptors;
    }

    public void addBean(final Object bean) {
        discoverDescriptors(bean);

        if (bean instanceof CliProcessorAware) {
            ((CliProcessorAware) bean).setProcessor(this);
        }
    }

    //
    // Discovery
    //

    private void discoverDescriptors(final Object bean) {
        assert bean != null;

        // Recursively process all the methods/fields (@Inherited won't work here)
        for (Class<?> type = bean.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                discoverDescriptor(bean, method);
            }
            for (Field field : type.getDeclaredFields()) {
                discoverDescriptor(bean, field);
            }
        }

        // Sanity check the argument indexes
        for (int i = 0; i < argumentDescriptors.size(); i++) {
            if (argumentDescriptors.get(i) == null) {
                throw new IllegalAnnotationError("No @Argument for index: " + i);
            }
        }
    }

    private void discoverDescriptor(final Object bean, final AnnotatedElement element) {
        assert bean != null;
        assert element != null;

        Option opt = element.getAnnotation(Option.class);
        Argument arg = element.getAnnotation(Argument.class);

        if (opt != null && arg != null) {
            throw new IllegalAnnotationError("Element can only implement @Option or @Argument, not both: " + element);
        }

        if (opt != null) {
            log.trace("Discovered @Option for: {}", element);

            OptionDescriptor desc = new OptionDescriptor(opt, SetterFactory.create(element, bean));

            // Make sure we have unique names
            for (OptionDescriptor tmp : optionDescriptors) {
                if (desc.getName().equals(tmp.getName())) {
                    throw new IllegalAnnotationError("Duplicate @Option name: " + desc.getName() + ", on: " + element);
                }
                if (desc.getLongName() != null && desc.getLongName().equals(tmp.getLongName())) {
                    throw new IllegalAnnotationError("Duplicate @Option longName: " + desc.getLongName() + ", on: " + element);
                }
            }

            optionDescriptors.add(desc);
        }
        else if (arg != null) {
            log.trace("Discovered @Argument for: {}", element);

            ArgumentDescriptor desc = new ArgumentDescriptor(arg, SetterFactory.create(element, bean));
            int index = arg.index();

            // Make sure the argument will fit in the list
            while (index >= argumentDescriptors.size()) {
                argumentDescriptors.add(null);
            }

            if (argumentDescriptors.get(index) != null) {
                throw new IllegalAnnotationError("Duplicate @Argument index: " + index + ", on: " + element);
            }

            argumentDescriptors.set(index, desc);
        }
    }

    //
    // Processing
    //

    private static class Parser
        extends PosixParser
    {
        @Override
        protected void checkRequiredOptions() {
            // delay, need to check for required options after processing to support override
        }

        public void ensureRequiredOptionsPresent() throws Exception {
            // FIXME: Probably want to use a custom exception here
            super.checkRequiredOptions();
        }
    }

    public void process(final String... args) throws Exception {
        assert args != null;

        Parser parser = new Parser();
        CommandLine cl;

        try {
            cl = parser.parse(createOptions(), args, stopAtNonOption);
        }
        catch (ParseException e) {
            throw new ProcessingException(e);    
        }

        Set<CliDescriptor> present = new HashSet<CliDescriptor>();
        boolean override = false;

        for (Object tmp : cl.getOptions()) {
            Opt opt = (Opt)tmp;
            log.trace("Processing option: {}", opt);

            OptionDescriptor desc = opt.getDescriptor();
            present.add(desc);

            // Track the override, this is used to handle when --help present, but a required arg/opt is missing
            if (!override) {
                override = desc.getOverride();
            }

            Handler handler = Handlers.create(desc);
            String[] values = opt.getValues();

            if (values == null || values.length == 0) {
                // Set the value
                handler.handle(opt.getValue());
            }
            else {
                // Set the values
                for (String value : values) {
                    handler.handle(value);
                }
            }
        }

        log.trace("Remaining arguments: {}", cl.getArgList());

        int i = 0;
        for (final String arg : cl.getArgs()) {
            log.trace("Processing argument: {}", arg);

            // Check if we allow an argument or we have overflowed
            if (i >= argumentDescriptors.size()) {
                throw new ProcessingException(argumentDescriptors.size() == 0 ? "No argument allowed" : "Too many arguments"); // TODO: i18n
            }

            ArgumentDescriptor desc = argumentDescriptors.get(i);
            present.add(desc);

            // For single-valued args, increment the argument index, else let the multivalued handler consume it
            if (!desc.isMultiValued()) {
                i++;
            }

            // Set the value
            Handler handler = Handlers.create(desc);
            handler.handle(arg);
        }

        // Check for any required arguments which were not present
        if (!override) {
            parser.ensureRequiredOptionsPresent();

            for (ArgumentDescriptor arg : argumentDescriptors) {
                if (arg.isRequired() && !present.contains(arg)) {
                    throw new ProcessingException("Argument is required: " + arg);
                }
            }
        }

        // TODO: Handle setting defaults
    }

    private Options createOptions() {
        Options opts = new Options();

        for (OptionDescriptor opt : optionDescriptors) {
            opts.addOption(new Opt(opt));
        }

        return opts;
    }

    private static class Opt
        extends org.apache.commons.cli.Option
    {
        private final OptionDescriptor desc;

        private Opt(final OptionDescriptor opt) throws IllegalArgumentException {
            super(opt.getName(), opt.getDescription());
            this.desc = opt;

            setLongOpt(opt.getLongName());
            setArgName(opt.getToken());
            setRequired(opt.isRequired());
            setValueSeparator(opt.getSeparator());
            setArgs(opt.getArgs());
            setOptionalArg(opt.isArgumentOptional());
        }

        public OptionDescriptor getDescriptor() {
            return desc;
        }

        public String toString() {
            return Yarn.render(this);
        }
    }
}