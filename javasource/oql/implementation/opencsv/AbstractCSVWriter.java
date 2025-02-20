package oql.implementation.opencsv;

/*
 Copyright 2015 Bytecode Pty Ltd.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 Modified by Mendix, removing ResultSetHelper variable and methods setResultService, writeColumnNames, writeAll(ResultSet, ...) and resultService
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * The AbstractCSVWriter was created to prevent duplication of code between the CSVWriter and the
 * CSVParserWriter classes.
 *
 * @since 4.2
 */
public abstract class AbstractCSVWriter implements ICSVWriter {

    protected final Writer writer;
    protected String lineEnd;
    protected volatile IOException exception;

    /**
     * Constructor to initialize the common values.
     *
     * @param writer  Writer used for output of csv data.
     * @param lineEnd String to append at end of data (either "\n" or "\r\n").
     */
    protected AbstractCSVWriter(Writer writer, String lineEnd) {
        this.writer = writer;
        this.lineEnd = lineEnd;
    }

    @Override
    public void writeAll(Iterable<String[]> allLines, boolean applyQuotesToAll) {
        StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
        try {
            for (String[] line : allLines) {
                writeNext(line, applyQuotesToAll, sb);
                sb.setLength(0);
            }
        } catch (IOException e) {
            exception = e;
        }
    }
    
    @Override
    public void writeNext(String[] nextLine, boolean applyQuotesToAll) {
        try {
            writeNext(nextLine, applyQuotesToAll, new StringBuilder(INITIAL_STRING_SIZE));
        } catch (IOException e) {
            exception = e;
        }
    }

    /**
     * Writes the next line to the file.  This method is a fail-fast method that will throw the
     * IOException of the writer supplied to the CSVWriter (if the Writer does not handle the exceptions itself like
     * the PrintWriter class).
     *
     * @param nextLine         a string array with each comma-separated element as a separate
     *                         entry.
     * @param applyQuotesToAll true if all values are to be quoted.  false applies quotes only
     *                         to values which contain the separator, escape, quote or new line characters.
     * @param appendable       Appendable used as buffer.
     * @throws IOException Exceptions thrown by the writer supplied to CSVWriter.
     */
    protected abstract void writeNext(String[] nextLine, boolean applyQuotesToAll, Appendable appendable) throws IOException;

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        writer.close();
    }

    @Override
    public boolean checkError() {

        if (writer instanceof PrintWriter) {
            PrintWriter pw = (PrintWriter) writer;
            return pw.checkError();
        }
        if (exception != null) {  // we don't want to lose the original exception
            flushQuietly();  // checkError in the PrintWriter class flushes the buffer so we shall too.
        } else {
            try {
                flush();
            } catch (IOException ioe) {
                exception = ioe;
            }
        }
        return exception != null;
    }

    @Override
    public IOException getException() {
        return exception;
    }

    @Override
    public void resetError() {
        exception = null;
    }
}
