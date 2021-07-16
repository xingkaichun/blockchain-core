package com.xingkaichun.helloworldblockchain.netcore.service;

import com.xingkaichun.helloworldblockchain.netcore.dao.NodeDao;
import com.xingkaichun.helloworldblockchain.netcore.model.Node;
import com.xingkaichun.helloworldblockchain.netcore.po.NodePo;
import com.xingkaichun.helloworldblockchain.setting.GenesisBlockSetting;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 邢开春 409060350@qq.com
 */
public class NodeServiceImpl implements NodeService {

    private NodeDao nodeDao;

    public NodeServiceImpl(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    @Override
    public void deleteNode(String ip){
        nodeDao.deleteNode(ip);
    }

    @Override
    public List<Node> queryAllNodes(){
        List<NodePo> nodePoList = nodeDao.queryAllNodeList();
        return nodeEntityConvertNode(nodePoList);
    }

    @Override
    public void addNode(Node node){
        if(queryNode(node.getIp()) != null){
            return;
        }
        NodePo nodePo = new NodePo();
        nodePo.setIp(node.getIp());
        long blockchainHeight = node.getBlockchainHeight()!=null?node.getBlockchainHeight(): GenesisBlockSetting.HEIGHT;
        nodePo.setBlockchainHeight(blockchainHeight);
        nodeDao.addNode(nodePo);
    }

    @Override
    public void updateNode(Node node){
        NodePo nodePo = nodeDao.queryNode(node.getIp());
        if(nodePo == null){
            return;
        }
        if(node.getBlockchainHeight() != null){
            nodePo.setBlockchainHeight(node.getBlockchainHeight());
        }
        nodeDao.updateNode(nodePo);
    }

    @Override
    public Node queryNode(String ip){
        NodePo nodePo = nodeDao.queryNode(ip);
        return nodeEntityConvertNode(nodePo);
    }

    private List<Node> nodeEntityConvertNode(List<NodePo> nodePoList){
        List<Node> nodeList = new ArrayList<>();
        if(nodePoList != null){
            for(NodePo nodePo : nodePoList){
                Node node = nodeEntityConvertNode(nodePo);
                nodeList.add(node);
            }
        }
        return nodeList;
    }

    private Node nodeEntityConvertNode(NodePo nodePo){
        if(nodePo == null){
            return null;
        }
        Node node = new Node();
        node.setIp(nodePo.getIp());
        node.setBlockchainHeight(nodePo.getBlockchainHeight());
        return node;
    }
}
