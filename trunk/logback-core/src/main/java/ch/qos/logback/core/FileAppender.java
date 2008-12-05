/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import ch.qos.logback.core.util.FileUtil;

/**
 * FileAppender appends log events to a file.
 * 
 * For more informatio about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#FileAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class FileAppender<E> extends WriterAppender<E> {

  /**
   * Append to or truncate the file? The default value for this variable is
   * <code>true</code>, meaning that by default a <code>FileAppender</code>
   * will append to an existing file and not truncate it.
   */
  protected boolean append = true;

  /**
   * The name of the active log file.
   */
  protected String fileName = null;

  /**
   * Do we do bufferedIO?
   */
  protected boolean bufferedIO = false;

  /**
   * The size of the IO buffer. Default is 8K.
   */
  protected int bufferSize = 8 * 1024;

  private boolean prudent = false;

  private FileChannel fileChannel = null;

  /**
   * As in most cases, the default constructor does nothing.
   */
  public FileAppender() {
  }

  /**
   * The <b>File</b> property takes a string value which should be the name of
   * the file to append to.
   */
  public void setFile(String file) {
    if(file == null) {
      fileName = file;
    } else {
      // Trim spaces from both ends. The users probably does not want
      // trailing spaces in file names.
      String val = file.trim();
      fileName = val;      

    }

  }

  /**
   * @deprecated Use isAppend instead
   */
  public boolean getAppend() {
    return append;
  }

  /**
   * Returns the value of the <b>Append</b> property.
   */
  public boolean isAppend() {
    return append;
  }

  
  /**
   * This method is used by derived classes to obtain the raw file property.
   * Regular users should not be using calling method.
   * 
   * @return the value of the file property
   */
  final public String rawFileProperty() {
    return fileName;
  }

  /**
   * Returns the value of the <b>File</b> property.
   * 
   * <p>This method may be overridden by derived classes.
   * 
   */
  public String getFile() {
    return fileName;
  }

  /**
   * If the value of <b>File</b> is not <code>null</code>, then
   * {@link #openFile} is called with the values of <b>File</b> and <b>Append</b>
   * properties.
   */
  public void start() {
    int errors = 0;
    if (getFile() != null) {
      addInfo("File property is set to [" + fileName + "]");

      if (prudent) {
        if (isAppend() == false) {
          setAppend(true);
          addWarn("Setting \"Append\" property to true on account of \"Prudent\" mode");
        }
        if (getImmediateFlush() == false) {
          setImmediateFlush(true);
          addWarn("Setting \"ImmediateFlush\" to true on account of \"Prudent\" mode");
        }

        if (bufferedIO == true) {
          setBufferedIO(false);
          addWarn("Setting \"BufferedIO\" property to false on account of \"Prudent\" mode");
        }
      }

      // In case both bufferedIO and immediateFlush are set, the former
      // takes priority because 'immediateFlush' is set to true by default.
      // If the user explicitly set bufferedIO, then we should follow her
      // directives.
      if (bufferedIO) {
        setImmediateFlush(false);
        addInfo("Setting \"ImmediateFlush\" property to false on account of \"bufferedIO\" property");
      }

      try {
        openFile(getFile());
      } catch (java.io.IOException e) {
        errors++;
        addError("openFile(" + fileName + "," + append + ") call failed.", e);
      }
    } else {
      errors++;
      addError("\"File\" property not set for appender named [" + name + "].");
    }
    if (errors == 0) {
      super.start();
    }
  }

  /**
   * <p> Sets and <i>opens</i> the file where the log output will go. The
   * specified file must be writable.
   * 
   * <p> If there was already an opened file, then the previous file is closed
   * first.
   * 
   * <p> <b>Do not use this method directly. To configure a FileAppender or one
   * of its subclasses, set its properties one by one and then call start().</b>
   * 
   * @param filename
   *                The path to the log file.
   * @param append
   *                If true will append to fileName. Otherwise will truncate
   *                fileName.
   * @param bufferedIO
   * @param bufferSize
   * 
   * @throws IOException
   * 
   */
  public synchronized void openFile(String file_name) throws IOException {
    File file = new File(file_name);
    if (FileUtil.mustCreateParentDirectories(file)) {
      boolean result = FileUtil.createMissingParentDirectories(file);
      if (!result) {
        addError("Failed to create parent directories for ["
            + file.getAbsolutePath() + "]");
      }
    }

    FileOutputStream fileOutputStream = new FileOutputStream(file_name, append);
    if (prudent) {
      fileChannel = fileOutputStream.getChannel();
    }
    Writer w = createWriter(fileOutputStream);
    if (bufferedIO) {
      w = new BufferedWriter(w, bufferSize);
    }
    setWriter(w);
  }

  public boolean isBufferedIO() {
    return bufferedIO;
  }

  public void setBufferedIO(boolean bufferedIO) {
    this.bufferedIO = bufferedIO;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  /**
   * @see #setPrudent(boolean)
   * 
   * @return true if in prudent mode
   */
  public boolean isPrudent() {
    return prudent;
  }

  /**
   * When prudent is set to true, file appenders from multiple JVMs can safely
   * write to the same file.
   * 
   * @param prudent
   */
  public void setPrudent(boolean prudent) {
    this.prudent = prudent;
  }

  public void setAppend(boolean append) {
    this.append = append;
  }

  final private void safeWrite(String s) throws IOException {
    FileLock fileLock = null;
    try {
      fileLock = fileChannel.lock();
      long position = fileChannel.position();
      long size = fileChannel.size();
      if (size != position) {
        fileChannel.position(size);
      }
      super.writerWrite(s, true);
    } finally {
      if (fileLock != null) {
        fileLock.release();
      }
    }
  }

  @Override
  protected void writerWrite(String s, boolean flush) throws IOException {
    if (prudent && fileChannel != null) {
      safeWrite(s);
    } else {
      super.writerWrite(s, flush);
    }
  }
}
