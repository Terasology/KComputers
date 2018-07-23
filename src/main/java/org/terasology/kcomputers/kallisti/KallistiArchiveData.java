/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.kcomputers.kallisti;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.terasology.assets.AssetData;
import org.terasology.kallisti.base.interfaces.FileSystem;
import org.terasology.kallisti.oc.OCFont;
import org.terasology.kcomputers.KComputersUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class KallistiArchiveData implements AssetData, FileSystem {
    public static class VirtualDirectory extends VirtualBase {
        private final Map<String, VirtualDirectory> subdirs;
        private final Map<String, VirtualFile> files;

        public VirtualDirectory(ZipEntry entry) {
            super(entry);
            this.subdirs = new TreeMap<>(Comparator.naturalOrder());
            this.files = new TreeMap<>(Comparator.naturalOrder());
        }

        public void addDirectory(VirtualDirectory directory) {
            this.subdirs.put(directory.name(), directory);
        }

        public void addFile(VirtualFile file) {
            this.files.put(file.name(), file);
        }

        public Optional<VirtualDirectory> getDirectory(String name) {
            return Optional.ofNullable(subdirs.get(name));
        }

        public Optional<VirtualFile> getFile(String name) {
            return Optional.ofNullable(files.get(name));
        }

        public Collection<VirtualDirectory> getDirectories() {
            return Collections.unmodifiableCollection(subdirs.values());
        }

        public Collection<VirtualFile> getFiles() {
            return Collections.unmodifiableCollection(files.values());
        }

        @Override
        public long size() {
            return 0;
        }
    }

    public static class VirtualFile extends VirtualBase {
        private final byte[] data;

        public VirtualFile(ZipEntry entry, InputStream stream) throws IOException {
            super(entry);

            data = KComputersUtil.toByteArray(stream);
        }

        public byte[] getData() {
            return data;
        }

        @Override
        public long size() {
            return data.length;
        }
    }

    private static abstract class VirtualBase implements FileSystem.Metadata {
        private final String path, name;
        private final Date dateCreate, dateModify;

        public VirtualBase(ZipEntry entry) {
            this.path = entry.getName().endsWith("/") ? entry.getName().substring(0, entry.getName().length() - 1) : entry.getName();
            String[] pathSplit = path.split("/");
            this.name = pathSplit[pathSplit.length - 1];

            if (entry.getCreationTime() != null) {
                this.dateCreate = new Date(entry.getCreationTime().toMillis());
            } else {
                this.dateCreate = new Date(0);
            }
            if (entry.getLastModifiedTime() != null) {
                this.dateModify = new Date(entry.getLastModifiedTime().toMillis());
            } else {
                this.dateModify = new Date(0);
            }
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public boolean isDirectory() {
            return (this instanceof VirtualDirectory);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public Date creationTime() {
            return dateCreate;
        }

        @Override
        public Date modificationTime() {
            return dateModify;
        }
    }

    public static class ByteArrayFile implements File {
        private final byte[] data;
        private long pos;

        public ByteArrayFile(byte[] data) {
            this.data = data;
            this.pos = 0;
        }

        @Override
        public boolean isSeekable() {
            return true;
        }

        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public long seek(Whence whence, int offset) throws IOException {
            switch (whence) {
                case CURRENT:
                    pos += offset;
                    break;
                case BEGINNING:
                    pos = offset;
                    break;
                case END:
                    pos = data.length + offset;
                    break;
            }
            return pos;
        }

        @Override
        public byte[] read(int bytes) throws IOException {
            byte[] d = new byte[Math.max(Math.min((int) (data.length - pos), bytes), 0)];
            if (d.length > 0) {
                System.arraycopy(data, (int) pos, d, 0, d.length);
                pos += d.length;
            }
            return d;
        }

        @Override
        public boolean write(byte[] value, int offset, int len) throws IOException {
            return false;
        }

        @Override
        public void close() throws Exception {

        }
    }

    private final VirtualDirectory root;

    public KallistiArchiveData(ZipInputStream stream) throws IOException {
        ZipEntry fakeRootEntry = new ZipEntry("/");
        root = new VirtualDirectory(fakeRootEntry);

        ZipEntry entry;
        while ((entry = stream.getNextEntry()) != null) {
            Optional<Metadata> dir = traverse(entry.getName(), 1);
            if (dir.isPresent() && dir.get() instanceof VirtualDirectory) {
                if (entry.isDirectory()) {
                    ((VirtualDirectory) dir.get()).addDirectory(new VirtualDirectory(entry));
                } else {
                    ((VirtualDirectory) dir.get()).addFile(new VirtualFile(entry, stream));
                }
            }
        }
    }

    private Optional<FileSystem.Metadata> traverse(String path, int ignore) {
        VirtualDirectory dir = root;
        String[] str = path.split("/");
        List<String> lstr = new ArrayList<>();
        for (String s : str) {
            if (!s.trim().isEmpty()) {
                lstr.add(s);
            }
        }

        for (int i = 0; i < lstr.size() - ignore; i++) {
            String s = lstr.get(i);
            Optional<VirtualDirectory> vdir = dir.getDirectory(s);
            if (vdir.isPresent()) {
                dir = vdir.get();
            } else if (i == lstr.size() - ignore - 1) {
                Optional<VirtualFile> vfile = dir.getFile(s);
                return Optional.ofNullable(vfile.orElse(null));
            }
        }
        return Optional.ofNullable(dir);
    }

    @SuppressWarnings("unchecked")
    protected Optional<VirtualFile> traverseFile(String path) {
        Optional optional = traverse(path, 0);
        if (optional.isPresent() && optional.get() instanceof VirtualFile) {
            return (Optional<VirtualFile>) optional;
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    protected Optional<VirtualDirectory> traverseDir(String path) {
        Optional optional = traverse(path, 0);
        if (optional.isPresent() && optional.get() instanceof VirtualDirectory) {
            return (Optional<VirtualDirectory>) optional;
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Metadata> list(String path) throws IOException {
        Optional<VirtualDirectory> o = traverseDir(path);
        if (o.isPresent()) {
            List<Metadata> m = new ArrayList<>();
            m.addAll(o.get().getDirectories());
            m.addAll(o.get().getFiles());
            return m;
        } else {
            throw new FileNotFoundException(path);
        }
    }

    @Override
    public File open(String path, OpenMode mode) throws IOException {
        KComputersUtil.LOGGER.info("Trying to open '" + path + "' in " + mode.name());

        if (mode != OpenMode.READ) {
            throw new IOException("file is read-only");
        }

        Optional<VirtualFile> o = traverseFile(path);
        if (o.isPresent()) {
            return new ByteArrayFile(o.get().getData());
        } else {
            throw new FileNotFoundException(path);
        }
    }

    @Override
    public Metadata metadata(String path) throws FileNotFoundException {
        Optional<Metadata> md = traverse(path, 0);
        return md.orElseThrow(() -> new FileNotFoundException(path));
    }

    @Override
    public boolean createDirectory(String path) throws IOException {
        throw new IOException("filesystem is read-only");
    }

    @Override
    public boolean delete(String path) throws IOException {
        throw new IOException("filesystem is read-only");
    }

    @Override
    public long getTotalAreaBytes() {
        return 0;
    }

    @Override
    public long getUsedAreaBytes() {
        return 0;
    }

    public byte[] readFully(String path) {
        Optional<VirtualFile> o = traverseFile(path);
        if (o.isPresent()) {
            return o.get().data;
        } else {
            throw new RuntimeException("Could not find " + path);
        }
    }

    public String readFully(String path, Charset charset) {
        return new String(readFully(path), charset);
    }
}
