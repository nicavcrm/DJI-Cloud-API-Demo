package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author sean
 * @version 1.0
 * @date 2024/10/17
 */
public class WirelessLinkTopo {

    @JsonProperty("center_node")
    private CenterNode centerNode;

    @JsonProperty("leaf_nodes")
    private List<LeafNode> leafNodes;

    @JsonProperty("secret_code")
    private List<Integer> secretCode;

    public static class CenterNode {
        @JsonProperty("sdr_id")
        private Integer sdrId;

        private String sn;

        public Integer getSdrId() {
            return sdrId;
        }

        public CenterNode setSdrId(Integer sdrId) {
            this.sdrId = sdrId;
            return this;
        }

        public String getSn() {
            return sn;
        }

        public CenterNode setSn(String sn) {
            this.sn = sn;
            return this;
        }

        @Override
        public String toString() {
            return "CenterNode{" +
                    "sdrId=" + sdrId +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    public static class LeafNode {
        @JsonProperty("control_source_index")
        private Integer controlSourceIndex;

        @JsonProperty("sdr_id")
        private Integer sdrId;

        private String sn;

        private Boolean valid;

        public Integer getControlSourceIndex() {
            return controlSourceIndex;
        }

        public LeafNode setControlSourceIndex(Integer controlSourceIndex) {
            this.controlSourceIndex = controlSourceIndex;
            return this;
        }

        public Integer getSdrId() {
            return sdrId;
        }

        public LeafNode setSdrId(Integer sdrId) {
            this.sdrId = sdrId;
            return this;
        }

        public String getSn() {
            return sn;
        }

        public LeafNode setSn(String sn) {
            this.sn = sn;
            return this;
        }

        public Boolean getValid() {
            return valid;
        }

        public LeafNode setValid(Boolean valid) {
            this.valid = valid;
            return this;
        }

        @Override
        public String toString() {
            return "LeafNode{" +
                    "controlSourceIndex=" + controlSourceIndex +
                    ", sdrId=" + sdrId +
                    ", sn='" + sn + '\'' +
                    ", valid=" + valid +
                    '}';
        }
    }

    public CenterNode getCenterNode() {
        return centerNode;
    }

    public WirelessLinkTopo setCenterNode(CenterNode centerNode) {
        this.centerNode = centerNode;
        return this;
    }

    public List<LeafNode> getLeafNodes() {
        return leafNodes;
    }

    public WirelessLinkTopo setLeafNodes(List<LeafNode> leafNodes) {
        this.leafNodes = leafNodes;
        return this;
    }

    public List<Integer> getSecretCode() {
        return secretCode;
    }

    public WirelessLinkTopo setSecretCode(List<Integer> secretCode) {
        this.secretCode = secretCode;
        return this;
    }

    @Override
    public String toString() {
        return "WirelessLinkTopo{" +
                "centerNode=" + centerNode +
                ", leafNodes=" + leafNodes +
                ", secretCode=" + secretCode +
                '}';
    }
}