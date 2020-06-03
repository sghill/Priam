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

package com.netflix.priam.config;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class FakeConfiguration implements IConfiguration {

    private final String appName;
    private String restorePrefix = "";
    public Map<String, Object> fakeConfig;

    public Map<String, String> fakeProperties = new HashMap<>();

    public FakeConfiguration() {
        this("my_fake_cluster");
    }

    public FakeConfiguration(String appName) {
        this.appName = appName;
        fakeConfig = new HashMap<>();
        fakeConfig.put("auto_bootstrap", false);
    }

    public Object getFakeConfig(String key) {
        return fakeConfig.get(key);
    }

    public void setFakeConfig(String key, Object value) {
        fakeConfig.put(key, value);
    }

    @Override
    public boolean getAutoBoostrap() {
        return (Boolean) fakeConfig.getOrDefault("auto_bootstrap", false);
    }

    @Override
    public String getCassHome() {
        return "/tmp/priam";
    }

    @Override
    public void initialize() {}

    @Override
    public String getBackupLocation() {
        return "casstestbackup";
    }

    @Override
    public String getBackupPrefix() {
        return "TEST-netflix.platform.S3";
    }

    @Override
    public String getCassandraBaseDirectory() {
        return "target";
    }

    @Override
    public List<String> getRacs() {
        return Arrays.asList("az1", "az2", "az3");
    }

    @Override
    public String getSnitch() {
        return "org.apache.cassandra.locator.SimpleSnitch";
    }

    @Override
    public String getAppName() {
        return appName;
    }

    @Override
    public String getRestorePrefix() {
        return this.restorePrefix;
    }

    // For testing purposes only.
    public void setRestorePrefix(String restorePrefix) {
        this.restorePrefix = restorePrefix;
    }

    @Override
    public String getBackupCommitLogLocation() {
        return "cass/backup/cl/";
    }

    @Override
    public String getCassStartupScript() {
        return "/usr/bin/false";
    }

    @Override
    public int getRemediateDeadCassandraRate() {
        return 1;
    }

    @Override
    public String getSeedProviderName() {
        return "org.apache.cassandra.locator.SimpleSeedProvider";
    }

    @Override
    public int getBackupRetentionDays() {
        return 5;
    }

    @Override
    public String getYamlLocation() {
        return getCassHome() + "/conf/cassandra.yaml";
    }

    @Override
    public String getJVMOptionsFileLocation() {
        return "src/test/resources/conf/jvm.options";
    }

    public String getCommitLogBackupPropsFile() {
        return getCassHome() + "/conf/commitlog_archiving.properties";
    }

    public String getCassYamlVal(String priamKey) {
        return "";
    }

    @Override
    public boolean isPostRestoreHookEnabled() {
        return true;
    }

    @Override
    public String getPostRestoreHook() {
        return "echo";
    }

    @Override
    public String getPostRestoreHookHeartbeatFileName() {
        return System.getProperty("java.io.tmpdir") + File.separator + "postrestorehook.heartbeat";
    }

    @Override
    public String getPostRestoreHookDoneFileName() {
        return System.getProperty("java.io.tmpdir") + File.separator + "postrestorehook.done";
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return fakeProperties.getOrDefault(key, defaultValue);
    }

    @Override
    public String getMergedConfigurationDirectory() {
        return fakeProperties.getOrDefault("priam_test_config", "/tmp/priam_test_config");
    }

    @Override
    public ImmutableSet<String> getTunablePropertyFiles() {
        String path = new File(getYamlLocation()).getParentFile().getPath();
        return ImmutableSet.of(path + "/cassandra-rackdc.properties");
    }

    public String getRAC() {
        return "my_zone";
    }

    public String getDC() {
        return "us-east-1";
    }
}
