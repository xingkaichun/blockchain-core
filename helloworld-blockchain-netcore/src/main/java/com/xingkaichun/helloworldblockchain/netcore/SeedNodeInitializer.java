package com.xingkaichun.helloworldblockchain.netcore;

import com.xingkaichun.helloworldblockchain.netcore.model.Node;
import com.xingkaichun.helloworldblockchain.netcore.service.NetCoreConfiguration;
import com.xingkaichun.helloworldblockchain.netcore.service.NodeService;
import com.xingkaichun.helloworldblockchain.setting.NetworkSetting;
import com.xingkaichun.helloworldblockchain.util.LogUtil;
import com.xingkaichun.helloworldblockchain.util.SystemUtil;
import com.xingkaichun.helloworldblockchain.util.ThreadUtil;


/**
 * 种子节点初始化器
 * 春种一粒粟,秋收万颗子。春天播种一粒种子节点,秋天就可以收获很多的节点了。
 * 有了这粒种子节点，我们(节点搜寻器NodeSearcher)就可以向种子节点询问：“种子节点，种子节点，你知道其它存在的节点吗？”。
 * 种子节点就会回复：“我知道，有节点甲、节点乙、节点丙...”。同时，种子节点还会把你这个节点记录下来。
 * 有了节点甲，我们(节点搜寻器NodeSearcher)就可以向甲节点询问：“甲节点，甲节点，你知道其它存在的节点吗？”。同时，甲节点还会把你这个节点记录下来。
 * 有了节点乙，我们(节点搜寻器NodeSearcher)就可以向乙节点询问：“乙节点，乙节点，你知道其它存在的节点吗？”。同时，乙节点还会把你这个节点记录下来。
 * ...
 * 最终任意两个节点都将互联起来，组成了区块链网络。
 *
 * @author 邢开春 409060350@qq.com
 */
public class SeedNodeInitializer {

    private NetCoreConfiguration netCoreConfiguration;
    private NodeService nodeService;

    public SeedNodeInitializer(NetCoreConfiguration netCoreConfiguration, NodeService nodeService) {

        this.netCoreConfiguration = netCoreConfiguration;
        this.nodeService = nodeService;
    }

    public void start() {
        /*
         * 定时循环的将种子节点加入区块链系统。
         * 因为有的种子节点可能会发生故障，然后本地节点链接不上种子节点，就将种子节点丢弃。
         * 能作为种子节点的服务器，肯定会很快被修复正常的。所以定时循环的将种子节点加入区块链，保证与种子节点连接是通畅的。
         */
        new Thread(()->{
            while (true){
                try {
                    if(netCoreConfiguration.isAutoSearchNode()){
                        addSeedNode();
                    }
                    ThreadUtil.millisecondSleep(netCoreConfiguration.getAddSeedNodeTimeInterval());
                } catch (Exception e) {
                    SystemUtil.errorExit("定时将种子节点加入区块链网络出现异常",e);
                }
            }
        }).start();
    }

    /**
     * 添加种子节点
     */
    private void addSeedNode() {
        for(String nodeIp: NetworkSetting.SEED_NODES){
            Node node = nodeService.queryNode(nodeIp);
            if(node == null){
                if(netCoreConfiguration.isAutoSearchNode()){
                    node = new Node();
                    node.setIp(nodeIp);
                    nodeService.addNode(node);
                    LogUtil.debug("种子节点["+node.getIp()+"]加入了区块链网络。");
                }
            }
        }
    }
}
