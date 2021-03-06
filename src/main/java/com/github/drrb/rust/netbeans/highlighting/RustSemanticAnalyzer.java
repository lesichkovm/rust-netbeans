/**
 * Copyright (C) 2015 drrb
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.drrb.rust.netbeans.highlighting;

import com.github.drrb.rust.netbeans.rustbridge.RustHighlight;
import com.github.drrb.rust.netbeans.parsing.NetbeansRustParser.NetbeansRustParserResult;
import com.github.drrb.rust.netbeans.rustbridge.RustSemanticHighlighter;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 */
public class RustSemanticAnalyzer extends SemanticAnalyzer<NetbeansRustParserResult> {
    private static final Logger LOG = Logger.getLogger(RustSemanticAnalyzer.class.getName());
    private final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    @Override
    public void run(NetbeansRustParserResult result, SchedulerEvent event) {
        highlights.clear();
        cancelled.set(false);

        File sourceFile = FileUtil.toFile(result.getSnapshot().getSource().getFileObject());
        RustSemanticHighlighter highlighter = new RustSemanticHighlighter(sourceFile);
        try {
            List<RustHighlight> rawHighlights = highlighter.getHighlights(result.getResult());
            highlights.putAll(mapHighlights(rawHighlights));
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return new HashMap<>(highlights);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    private Map<OffsetRange, Set<ColoringAttributes>> mapHighlights(Collection<RustHighlight> highlights) {
        Map<OffsetRange, Set<ColoringAttributes>> highlightsMap = new HashMap<>(highlights.size());
        for (RustHighlight highlight : highlights) {
            highlightsMap.put(new OffsetRange(highlight.startChar, highlight.endChar), highlight.getKind().colors());
        }
        return highlightsMap;
    }
}
