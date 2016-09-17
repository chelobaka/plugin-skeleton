/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters;

import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.ITranslateCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import static org.testng.Assert.*;

/**
 * Base class for test filter parsing.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Hiroshi Miura
 */
abstract class TestFilterBase  {

    protected FilterContext context = new FilterContext();

    protected File outFile;

    protected void test(final IFilter filter, final String testcase) throws Exception {
        List<String> entries = parse(filter, "/" + testcase + ".txt");
        try (BufferedReader reader = getBufferedReader(new
                        File(this.getClass().getResource("/" + testcase + ".json").getFile()),
                "UTF-8")) {
            String jsonString = IOUtils.toString(reader);
            ArrayList expected = JSON.parseObject(jsonString, ArrayList.class);
            assertEquals(entries, expected, testcase);
        }
    }

    protected void testTranslate(final IFilter filter, final String testcase) throws Exception {
        translateText(filter, "/" + testcase + ".txt");
    }

    protected List<String> parse(IFilter filter, String resource) throws Exception {
        return parse(filter, resource, Collections.emptyMap());
    }

    protected List<String> parse(IFilter filter, String resource, Map<String, String> options)
            throws Exception {
        final List<String> result = new ArrayList<>();

        filter.parseFile(new File(this.getClass().getResource(resource).getFile()), options, context, new IParseCallback() {
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, IFilter filter) {
                addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
            }

            public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                                 String path, IFilter filter, List<ProtectedPart> protectedParts) {
                if (!source.isEmpty()) {
                    result.add(source);
                }
            }

            public void linkPrevNextSegments() {
            }
        });

        return result;
    }

    protected void translate(IFilter filter, String resource) throws Exception {
        translate(filter, resource, Collections.emptyMap());
    }
    
    protected void translate(IFilter filter, String resource, Map<String, String> config) throws Exception {
        outFile = File.createTempFile("output", ".txt");
        outFile.deleteOnExit();
        filter.translateFile(new File(this.getClass().getResource(resource).getFile()), outFile, config, context,
                new ITranslateCallback() {
                    public String getTranslation(String id, String source, String path) {
                        return source;
                    }

                    public String getTranslation(String id, String source) {
                        return source;
                    }

                    public void linkPrevNextSegments() {
                    }

                    public void setPass(int pass) {
                    }
                });
    }

    protected void align(IFilter filter, String in, String out, IAlignCallback callback) throws Exception {
        File inFile = new File("/data/filters/" + in);
        File outFile = new File("/data/filters/" + out);
        filter.alignFile(inFile, outFile, Collections.emptyMap(), context, callback);
    }

    protected void translateText(IFilter filter, String resource) throws Exception {
        translateText(filter, resource, Collections.emptyMap());
    }

    protected void translateText(IFilter filter, String resource, Map<String, String> config) throws Exception {
        translate(filter, resource, config);
        FileUtils.contentEquals(new File(this.getClass().getResource(resource).getFile()), outFile);
    }

    /**
     * Create BufferedReader from specified file and encoding.
     *
     * @param inFile file to read.
     * @param inEncoding file encoding.
     * @return BufferReader object.
     * @throws IOException when file I/O error happened.
     */
    private static BufferedReader getBufferedReader(final File inFile, final String inEncoding)
            throws IOException {
        InputStreamReader isr;
        if (inEncoding == null) {
            isr = new InputStreamReader(new FileInputStream(inFile), Charset.defaultCharset());
        } else {
            isr = new InputStreamReader(new FileInputStream(inFile), inEncoding);
        }
        return new BufferedReader(isr);
    }

}
