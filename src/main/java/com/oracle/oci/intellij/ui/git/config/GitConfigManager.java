package com.oracle.oci.intellij.ui.git.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;

public class GitConfigManager {

  private static GitConfigManager INSTANCE;
  private Map<String, GitConfig> cachedConfigs = new HashMap<>();
  private ExecutorService execService = Executors.newSingleThreadExecutor();

  public static synchronized GitConfigManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new GitConfigManager();
    }
    return INSTANCE;
  }

  public Future<GitConfig> getConfig(final Project project, Function<GitConfig, GitConfig> onComplete) {
    GitConfig gitConfig = cachedConfigs.get(project.getProjectFilePath());
    boolean reload = false;
    if (gitConfig != null) {
      reload = gitConfig.checkStale();
    }
    else {
      reload = true;
    }
    if (reload) {
      Future<GitConfig> submit = execService.submit(() -> createReloadCallable(project, onComplete));
      return submit;
    }
    GitConfig result = onComplete.apply(gitConfig);
    return CompletableFuture.completedFuture(result);
  }

  private GitConfig createReloadCallable(final Project project, Function<GitConfig, GitConfig> onComplete) {
    VirtualFile guessProjectDir = ProjectUtil.guessProjectDir(project);
    if (guessProjectDir != null && guessProjectDir.exists() && guessProjectDir.isDirectory()) {
      @Nullable
      VirtualFile gitConfigFile = guessProjectDir.findFileByRelativePath(".git/config");
      try {
        if (gitConfigFile != null && gitConfigFile.exists()) {
          @NotNull
          InputStream inputStream = gitConfigFile.getInputStream();
          Reader reader = new InputStreamReader(inputStream);
          GitParser parser = new GitParser();
          GitConfig gitConfig = parser.parse(reader);
          gitConfig.setConfigFile(gitConfigFile);
          GitConfig prevConfig = cachedConfigs.put(project.getProjectFilePath(), gitConfig);
          Optional.ofNullable(prevConfig).ifPresent(p -> p.dispose());
          return onComplete.apply(gitConfig);
        }
      }
      catch (Exception ioe) {
        ioe.printStackTrace();
      }
    }
    return null;
  }
}
