/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mybatch.operations;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.batch.operations.JobEndCallback;
import javax.batch.operations.JobOperator;
import javax.batch.operations.exception.CallbackRegistrationException;
import javax.batch.operations.exception.JobExecutionNotRunningException;
import javax.batch.operations.exception.JobInstanceAlreadyCompleteException;
import javax.batch.operations.exception.JobRestartException;
import javax.batch.operations.exception.JobStartException;
import javax.batch.operations.exception.NoSuchJobException;
import javax.batch.operations.exception.NoSuchJobExecutionException;
import javax.batch.operations.exception.NoSuchJobInstanceException;
import javax.batch.state.JobExecution;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.mybatch.creation.ArtifactFactory;
import org.mybatch.creation.SimpleArtifactFactory;
import org.mybatch.job.Job;
import org.mybatch.metadata.ApplicationMetaData;
import org.mybatch.repository.JobRepository;
import org.mybatch.repository.impl.MemoryRepository;
import org.mybatch.runtime.runner.JobExecutionRunner;
import org.mybatch.state.JobExecutionImpl;
import org.mybatch.state.JobInstanceImpl;
import org.mybatch.util.BatchUtil;

import static org.mybatch.util.BatchLogger.LOGGER;

public class JobOperatorImpl implements JobOperator {
    //TODO use factory
    JobRepository repository = new MemoryRepository();
    ArtifactFactory artifactFactory = new SimpleArtifactFactory();

    @Override
    public List<Long> getExecutions(long instanceId) throws NoSuchJobInstanceException {
        return null;
    }

    @Override
    public int getJobInstanceCount(String jobName) throws NoSuchJobException {
        return 0;
    }

    @Override
    public List<Long> getJobInstances(String jobName, int start, int count) throws NoSuchJobException {
        return null;
    }

    @Override
    public Set<Long> getRunningExecutions(String jobName) throws NoSuchJobException {
        return null;
    }

    @Override
    public Properties getParameters(long executionId) throws NoSuchJobExecutionException {
        return null;
    }

    @Override
    public Long start(String job, Properties jobParameters) throws JobStartException {
        InputStream is;
        Job jobDefined;
        try {
            is = getJobXml(job);
        } catch (IOException e) {
            throw LOGGER.failToGetJobXml(e, job);
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Job.class);
            Unmarshaller um = jaxbContext.createUnmarshaller();
            JAXBElement<Job> root = um.unmarshal(new StreamSource(is), Job.class);
            jobDefined = root.getValue();
        } catch (JAXBException e) {
            throw LOGGER.failToParseBindJobXml(e, job);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }

        repository.add(jobDefined);
        ApplicationMetaData appData;
        try {
            appData = new ApplicationMetaData();
        } catch (IOException e) {
            throw LOGGER.failToProcessMetaData(e, job);
        }
        JobInstanceImpl instance = new JobInstanceImpl(jobDefined, appData, artifactFactory);
        JobExecutionImpl jobExecution = new JobExecutionImpl(instance);
        JobExecutionRunner jobExecutionRunner = new JobExecutionRunner(jobDefined, instance, jobExecution);
        jobExecutionRunner.run();
        return jobExecution.getExecutionId();
    }

    @Override
    public Long restart(long executionId, Properties jobParameters) throws JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException {
        return null;
    }

    @Override
    public void stop(long executionId) throws NoSuchJobExecutionException, JobExecutionNotRunningException {

    }

    @Override
    public Set<String> getJobNames() {
        return null;
    }

    @Override
    public List<JobExecution> getJobExecutions(long instanceId) {
        return null;
    }

    @Override
    public JobExecution getJobExecution(long executionId) {
        return null;
    }

    @Override
    public long registerJobEndCallback(JobEndCallback callback) throws CallbackRegistrationException {
        return 0;
    }

    private InputStream getJobXml(String jobXml) throws IOException {
        // META-INF first
        String path = "META-INF/" + jobXml;
        InputStream is = null;
        is = BatchUtil.getBatchApplicationClassLoader().getResourceAsStream(path);
        if (is != null) {
            return is;
        }

        // javax.jobpath system property. jobpath format?
        File jobFile = null;
        String jobpath = System.getProperty("javax.jobpath");
        if (jobpath != null && !jobpath.isEmpty()) {
            String[] jobPathElements = jobpath.split(":");
            for (String p : jobPathElements) {
                jobFile = new File(p, jobXml);
                if (jobFile.exists() && jobFile.isFile()) {
                    break;
                }
            }
        }

        // default location: current directory
        if (jobFile == null) {
            jobFile = new File(System.getProperty("user.dir"), jobXml);
        }

        is = new BufferedInputStream(new FileInputStream(jobFile));
        return is;
    }

}
