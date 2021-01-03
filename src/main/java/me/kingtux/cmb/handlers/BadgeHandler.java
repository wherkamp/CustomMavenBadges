package me.kingtux.cmb.handlers;

import io.javalin.http.Context;
import me.kingtux.cmb.BadgeUtils;
import me.kingtux.cmb.MavenHelper;
import me.kingtux.cmb.MavenUtils;
import me.kingtux.cmb.maven.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class BadgeHandler {
    private MavenHelper mavenHelper;

    public BadgeHandler(MavenHelper mavenHelper) {
        this.mavenHelper = mavenHelper;
    }


    public void getBadge(Context context) {
        Optional<Repository> repositoryOptional = mavenHelper.getResolver().getRepository(context.pathParam("repo"));
        if (repositoryOptional.isEmpty()) {
            //TODO improve this
            context.status(404);
            return;
        }
        Repository repository = repositoryOptional.get();
        String groupID = context.pathParam("group");
        String artifact = context.pathParam("artifact");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(repository.getURL()).append("/");
        stringBuilder.append(groupID.replace(".", "/")).append("/");
        stringBuilder.append(artifact).append("/");
        stringBuilder.append("maven-metadata.xml");
        String path = stringBuilder.toString();
        try {
            String latestVersion = MavenUtils.getLatestVersion(path);
            File badge = BadgeUtils.getBadge(latestVersion, repository.getName(), mavenHelper.getConfig().getColor());
            context.contentType("image/png");
            context.result(new FileInputStream(badge));
        } catch (ExecutionException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
