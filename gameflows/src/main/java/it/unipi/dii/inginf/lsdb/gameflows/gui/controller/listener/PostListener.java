package it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener;

import it.unipi.dii.inginf.lsdb.gameflows.post.Post;

public interface PostListener {
	public void onClickPost(javafx.scene.input.MouseEvent mouseEvent, Post post);
}
