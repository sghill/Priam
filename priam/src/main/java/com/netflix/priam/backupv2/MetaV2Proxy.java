/*
 * Copyright 2018 Netflix, Inc.
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
 *
 */

package com.netflix.priam.backupv2;

import com.google.inject.Provider;
import com.netflix.priam.backup.AbstractBackupPath;
import com.netflix.priam.backup.BackupRestoreException;
import com.netflix.priam.backup.IBackupFileSystem;
import com.netflix.priam.backup.IFileSystemContext;
import com.netflix.priam.config.IConfiguration;
import com.netflix.priam.utils.DateUtil;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Do any management task for meta files. Created by aagrawal on 8/2/18. */
public class MetaV2Proxy implements IMetaProxy {
    private static final Logger logger = LoggerFactory.getLogger(MetaV2Proxy.class);
    private final Path metaFileDirectory;
    private final IBackupFileSystem fs;
    private final Provider<AbstractBackupPath> abstractBackupPathProvider;

    @Inject
    MetaV2Proxy(
            IConfiguration configuration,
            IFileSystemContext backupFileSystemCtx,
            Provider<AbstractBackupPath> abstractBackupPathProvider) {
        fs = backupFileSystemCtx.getFileStrategy(configuration);
        this.abstractBackupPathProvider = abstractBackupPathProvider;
        metaFileDirectory = Paths.get(configuration.getDataFileLocation());
    }

    @Override
    public Path getLocalMetaFileDirectory() {
        return metaFileDirectory;
    }

    @Override
    public String getMetaPrefix(DateUtil.DateRange dateRange) {
        Path location = fs.getPrefix();
        AbstractBackupPath abstractBackupPath = abstractBackupPathProvider.get();
        String match = StringUtils.EMPTY;
        if (dateRange != null) match = dateRange.match();
        return Paths.get(
                        abstractBackupPath
                                .remoteV2Prefix(location, AbstractBackupPath.BackupFileType.META_V2)
                                .toString(),
                        match)
                .toString();
    }

    @Override
    public List<AbstractBackupPath> findMetaFiles(DateUtil.DateRange dateRange) {
        ArrayList<AbstractBackupPath> metas = new ArrayList<>();
        String prefix = getMetaPrefix(dateRange);
        String marker = getMetaPrefix(new DateUtil.DateRange(dateRange.getStartTime(), null));
        logger.info(
                "Listing filesystem with prefix: {}, marker: {}, daterange: {}",
                prefix,
                marker,
                dateRange);
        Iterator<String> iterator = fs.listFileSystem(prefix, null, marker);

        while (iterator.hasNext()) {
            AbstractBackupPath abstractBackupPath = abstractBackupPathProvider.get();
            abstractBackupPath.parseRemote(iterator.next());
            logger.debug("Meta file found: {}", abstractBackupPath);
            if (abstractBackupPath.getLastModified().toEpochMilli()
                            >= dateRange.getStartTime().toEpochMilli()
                    && abstractBackupPath.getLastModified().toEpochMilli()
                            <= dateRange.getEndTime().toEpochMilli()) {
                metas.add(abstractBackupPath);
            }
        }

        Collections.sort(metas, Collections.reverseOrder());

        if (metas.size() == 0) {
            logger.info(
                    "No meta file found on remote file system for the time period: {}", dateRange);
        }

        return metas;
    }

    @Override
    public Path downloadMetaFile(AbstractBackupPath meta) throws BackupRestoreException {
        Path localFile = Paths.get(meta.newRestoreFile().getAbsolutePath());
        fs.downloadFile(Paths.get(meta.getRemotePath()), localFile, 10);
        return localFile;
    }

    @Override
    public void cleanupOldMetaFiles() {
        logger.info("Deleting any old META_V2 files if any");
        IOFileFilter fileNameFilter =
                FileFilterUtils.and(
                        FileFilterUtils.prefixFileFilter(MetaFileInfo.META_FILE_PREFIX),
                        FileFilterUtils.or(
                                FileFilterUtils.suffixFileFilter(MetaFileInfo.META_FILE_SUFFIX),
                                FileFilterUtils.suffixFileFilter(
                                        MetaFileInfo.META_FILE_SUFFIX + ".tmp")));
        Collection<File> files =
                FileUtils.listFiles(metaFileDirectory.toFile(), fileNameFilter, null);
        files.stream()
                .filter(File::isFile)
                .forEach(
                        file -> {
                            logger.debug(
                                    "Deleting old META_V2 file found: {}", file.getAbsolutePath());
                            file.delete();
                        });
    }

    @Override
    public List<String> getSSTFilesFromMeta(Path localMetaPath) throws Exception {
        return null;
    }
}
