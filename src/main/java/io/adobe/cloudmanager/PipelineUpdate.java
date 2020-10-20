package io.adobe.cloudmanager;

public class PipelineUpdate {

    private String branch;
    private String repositoryId; // TODO: What is this?

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PipelineUpdate that = (PipelineUpdate) o;

        if (branch != null ? !branch.equals(that.branch) : that.branch != null) return false;
        return repositoryId != null ? repositoryId.equals(that.repositoryId) : that.repositoryId == null;
    }

    @Override
    public int hashCode() {
        int result = branch != null ? branch.hashCode() : 0;
        result = 31 * result + (repositoryId != null ? repositoryId.hashCode() : 0);
        return result;
    }
}
