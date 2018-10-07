import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.omg.CORBA.INVALID_TRANSACTION;

public class AdaBoosting {
	
	private String fileName;
	private int numOfAttributes=9;
	private final int numOfAttribValues=10;
	private final int percentOfTrain=80;
	private final int Ntimes=100;
	private final int Stump=1;
	private Random random;
	private Node root;
	
	private int numOfData;
    private int numOfTrainData;
    private int numOfTestData;
    
    double accuracyAvgDT,precisionAvgDT,recallAvgDT,f_measureAvgDT,g_meanAvgDT;
    double accuracyAvgAB,precisionAvgAB,recallAvgAB,f_measureAvgAB,g_meanAvgAB;
    
    CopyOnWriteArrayList<Double> averageStump;
    CopyOnWriteArrayList<Double> averageAB;
    
	CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> data;
	CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> trainData;
	CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> testData;
	CopyOnWriteArrayList<Double> weight;
	CopyOnWriteArrayList<DecisionStump> treeList;
	CopyOnWriteArraySet<Integer> ASet;
	
	public AdaBoosting(String fileName) throws FileNotFoundException {
		this.fileName=fileName;
		
		random=new Random();
		accuracyAvgDT=precisionAvgDT=recallAvgDT=f_measureAvgDT=g_meanAvgDT=0;
		ASet=new CopyOnWriteArraySet<Integer>();
        for(int j=0;j<numOfAttributes;j++){
            ASet.add(j);
        }
		
        averageStump=new CopyOnWriteArrayList<Double>();
        averageAB=new CopyOnWriteArrayList<Double>();
        
		data=new CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>();
		trainData=new CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>();
        testData=new CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>();
		weight=new CopyOnWriteArrayList<Double>();
		treeList= new CopyOnWriteArrayList<DecisionStump>();
		
		readData(this.fileName);
		numOfData=data.size();
		
//		CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> temp=new CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>();
//		for(int i=0;i<data.size();i++){
//			temp.add(data.get(i));
//		}
		
		
		//selectTrainAndTestData(percentOfTrain);
		
		/*DecisionStump ds=decisionStampID3(A, trainData);
		System.out.println(ds.classVal);
		System.out.println(ds.attribute);
		treeList.add(ds);
		*/
		//adaBoost(trainData, 2 );
//		for(int i=0;i<treeList.size();i++){
//			System.out.println(treeList.get(i).classVal);
//			System.out.println(treeList.get(i).treeWeight);
//		}
		
		//test(testData);
		compareMethods();
		compareRounds();
	}
	
	private void compareRounds(){
		System.out.println("******************************************************");
		System.out.println("Comparing Rounds");
		System.out.println("******************************************************");
		
		int[] roundVals={5,10,20,30};
		CopyOnWriteArrayList<Integer> rounds=new CopyOnWriteArrayList<Integer>();
		CopyOnWriteArrayList<CopyOnWriteArrayList<Double>> average=new CopyOnWriteArrayList<CopyOnWriteArrayList<Double>>();
		
		int comp=roundVals.length;
		for(int i=0;i<comp;i++){
			rounds.add(roundVals[i]);
			CopyOnWriteArrayList<Double> temp=new CopyOnWriteArrayList<Double>();
			for(int j=0;j<5;j++){
				temp.add(0.0);
			}
			average.add(temp);
		}
		
		
		
		for(int i=0;i<Ntimes;i++){
			trainData.clear();
            testData.clear();
            weight.clear();
            treeList.clear();
            
            selectTrainAndTestData(percentOfTrain);
            
            //adaboost different rounds
            for(int j=0;j<comp;j++){
	            adaBoost(trainData, roundVals[j]);
	            //System.out.println("j:"+j);
	            test(testData,average.get(j));
	            weight.clear();
	            treeList.clear();
            }
            
		}
		
		for(int j=0;j<comp;j++){
			System.out.println("\nAdaBoosting ("+roundVals[j]+" rounds): ");
	        System.out.printf("Accuracy= \t%.12f\n",average.get(j).get(0)/Ntimes);
	        System.out.printf("Precision= \t%.12f\n",average.get(j).get(1)/Ntimes);
	        System.out.printf("Recall= \t%.12f\n",average.get(j).get(2)/Ntimes);
	        System.out.printf("F-measure= \t%.12f\n",average.get(j).get(3)/Ntimes);
	        System.out.printf("G-mean= \t%.12f\n",average.get(j).get(4)/Ntimes);
		}
		
	}
	
	private void compareMethods(){
		System.out.println("******************************************************");
		System.out.println("Comparing Methods");
		System.out.println("******************************************************");
		int i;
		for(i=0;i<5;i++){
			averageStump.add(0.0);
			averageAB.add(0.0);
		}
        for(i=0;i<Ntimes;i++){
            //clear for each iteration
            trainData.clear();
            testData.clear();
            
            weight.clear();
            treeList.clear();
            
            //start actual work
            selectTrainAndTestData(percentOfTrain);
            CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> example;
            example=trainData;
            CopyOnWriteArraySet<Integer> A=new CopyOnWriteArraySet<Integer>();
            for(int j=0;j<numOfAttributes;j++){
                A.add(j);
            }
            root=new Node();
            root.parent=null;
            
            //decision stump
            adaBoost(example, Stump);
            test(testData,averageStump);
            
            weight.clear();
            treeList.clear();
            
            //adaboost 30 rounds
            adaBoost(example, 30);
            test(testData,averageAB);
            
            //id3 decision tree
            ID3(root, A, example);
            testDecisionTree(testData);
            
        }
        accuracyAvgDT/=Ntimes;
        precisionAvgDT/=Ntimes;
        recallAvgDT/=Ntimes;
        f_measureAvgDT/=Ntimes;
        g_meanAvgDT/=Ntimes;
        
        /*accuracyAvgAB/=Ntimes;
        precisionAvgAB/=Ntimes;
        recallAvgAB/=Ntimes;
        f_measureAvgAB/=Ntimes;
        g_meanAvgAB/=Ntimes;
        */
        
        System.out.println("Decision Stump: ");
        System.out.printf("Accuracy= \t%.12f\n",averageStump.get(0)/Ntimes);
        System.out.printf("Precision= \t%.12f\n",averageStump.get(1)/Ntimes);
        System.out.printf("Recall= \t%.12f\n",averageStump.get(2)/Ntimes);
        System.out.printf("F-measure= \t%.12f\n",averageStump.get(3)/Ntimes);
        System.out.printf("G-mean= \t%.12f\n",averageStump.get(4)/Ntimes);
        
        System.out.println("\nID3 Decision Tree: ");
        System.out.printf("Accuracy= \t%.12f\n",accuracyAvgDT);
        System.out.printf("Precision= \t%.12f\n",precisionAvgDT);
        System.out.printf("Recall= \t%.12f\n",recallAvgDT);
        System.out.printf("F-measure= \t%.12f\n",f_measureAvgDT);
        System.out.printf("G-mean= \t%.12f\n",g_meanAvgDT);
        
        System.out.println("\nAdaBoosting (30 rounds): ");
        System.out.printf("Accuracy= \t%.12f\n",averageAB.get(0)/Ntimes);
        System.out.printf("Precision= \t%.12f\n",averageAB.get(1)/Ntimes);
        System.out.printf("Recall= \t%.12f\n",averageAB.get(2)/Ntimes);
        System.out.printf("F-measure= \t%.12f\n",averageAB.get(3)/Ntimes);
        System.out.printf("G-mean= \t%.12f\n",averageAB.get(4)/Ntimes);
	}
	
	private void readData(String fileName) throws FileNotFoundException{
        String line="";
        Scanner in = new Scanner(new FileReader(new File(fileName)));
        
        while(in.hasNextLine()){
            line=in.nextLine();
            line=line.trim();
            String[] split=line.split(",");
            CopyOnWriteArrayList<Integer> temp=new CopyOnWriteArrayList<Integer>();
            for(int l=0;l<numOfAttributes+1;l++){
                temp.add(Integer.parseInt(split[l]));
            }
            data.add(temp);
        }
        in.close();
    }
	
	private void adaBoost(CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> train,int k){
		int trainSize=train.size();
		double initWeight=1.0/trainSize;
		for(int i=0;i<trainSize;i++){//initial weight
			weight.add(initWeight);
		}
		
		double totalWeight=1.0;
		
		for(int r=1;r<=k;r++){
			CopyOnWriteArrayList<Double> pr=new CopyOnWriteArrayList<Double>();
			for(int i=0;i<trainSize;i++){
				pr.add(weight.get(i)/totalWeight);
			}

			DecisionStump ds=learn(pr, train);
			treeList.add(ds);
			double er=calculateError(ds, pr, train);
			if(er>0.5){
				k=r-1;
				break;
			}
			//System.out.println("er="+er);
			double br=er/(1-er);
			//System.out.println("br="+br);
			ds.treeWeight=Math.log(1/br);
			totalWeight=updateWeight(ds, br, train);
		}
	}
	private double updateWeight(DecisionStump ds, double br, CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> train){
		int size=train.size();
		int index=ds.attribute;
		for(int i=0;i<size;i++){
			if(ds.classVal.get(train.get(i).get(index))==train.get(i).get(numOfAttributes)){
				double newWeight=weight.get(i)*br;
				weight.set(i, newWeight);
			}
		}
		double totalWeight=0;
		for(int i=0;i<size;i++){
			totalWeight+=weight.get(i);
		}
		return totalWeight;
	}
	
	private double calculateError(DecisionStump ds, CopyOnWriteArrayList<Double> pr, CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> train){
		double E=0;
		int size=train.size();
		int index=ds.attribute;
		for(int i=0;i<size;i++){
			if(ds.classVal.get(train.get(i).get(index))!=train.get(i).get(numOfAttributes)){
				E+=pr.get(i);
			}
		}
		return E;
	}
	
	private DecisionStump learn(CopyOnWriteArrayList<Double> pr, CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> train){
		//select from train according to weight.. then call decisionStump
		CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> example=new CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>();
		double sumWeight=0;
		int trainSize=train.size();
		CopyOnWriteArrayList<Double> prSum=new CopyOnWriteArrayList<Double>();
		for(int i=0;i<trainSize;i++){
			sumWeight+=pr.get(i);
			prSum.add(sumWeight);
		}
		//System.out.println("sumWeight: "+prSum.get(prSum.size()-1));
		double rand;
		int newSize=0;
		while(newSize<trainSize){
			rand=random.nextDouble();
			int index=searchIndex(prSum,rand);
			example.add(train.get(index));
			newSize++;
		}
		
		
		
		/*
		int size=train.size();
		double rand;
		int newSize=0;
		
		while(newSize<size){
			rand=random.nextDouble();
			//System.out.println(rand);
			for(int i=0;i<size;i++){
				if(pr.get(i)>rand){
					example.add(train.get(i));
					newSize++;
					if(newSize==size){
						break;
					}
				}
			}
		}
		*/
		
		DecisionStump ds=decisionStampID3(ASet, example);
		return ds;
	}
	
	private int searchIndex(CopyOnWriteArrayList<Double> prSum, double val){
		int index;
		int first=0;
		int last=prSum.size()-1;
		int mid=0;
		while(first<last){
			mid=(first+last)/2;
			if(val<=prSum.get(mid)){
				last=mid;
			}
			else{
				first=mid+1;
			}
		}
		index=mid;
		if(mid!=0 && mid!=prSum.size()-1){
			if(val<=prSum.get(mid-1)){
				index=mid-1;
			}
			if(val>prSum.get(mid)){
				index=mid+1;
			}
		}
		else if(mid==0){
			if(val>prSum.get(mid)){
				index=mid+1;
			}
		}
		else if(mid==prSum.size()-1){
			if(val<=prSum.get(mid-1)){
				index=mid-1;
			}
		}
		
		return index;
	}
	
	private DecisionStump decisionStampID3(CopyOnWriteArraySet<Integer> attributes, CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> example){
		DecisionStump dStump=new DecisionStump();;
		int i,target_attrib;
		int size=example.size();
		int val=example.get(0).get(numOfAttributes);
        for(i=1;i<size;i++){
            if(example.get(i).get(numOfAttributes)!=val){//check class
                break;
            }
        }
        if(i==size){//all examples are in same class with value 'val'
        	//create a decisionStump
        	//dStump=new DecisionStump();
        	dStump.attribute=0;
        	for(int j=0;i<numOfAttribValues;j++){
        		dStump.classVal.put(j+1, val);
        	}
        	return dStump;
        }
        
        //not all examples are same
        int data0=0,data1=0;
        for(i=0;i<size;i++){
            if(example.get(i).get(numOfAttributes)==0){//check class
                data0++;
            }
            else{
                data1++;
            }
        }
        if(data0>=data1){
            target_attrib=0;
        }
        else{
            target_attrib=1;
        }
        
        double currentEntropy=calculateEntropy(example);
        int index=bestAttribute(currentEntropy, attributes, example);//decision attribute
        
        dStump.attribute=index;
        
        CopyOnWriteArrayList<CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>> splittedData;
        splittedData=new CopyOnWriteArrayList<CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>>();
        
        for(i=0;i<numOfAttribValues;i++){
            CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> temp;
            temp=splitData(i+1,index,example);
            splittedData.add(temp);
        }

        for(i=0;i<numOfAttribValues;i++){
            CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> temp;
            temp=splittedData.get(i);
            
            if(temp.size()==0){
                //set with target attribute
            	dStump.classVal.put(i+1, target_attrib);
            }
            else{
            	int classVal=getClassValue(temp);
            	dStump.classVal.put(i+1, classVal);
            }
            
        }
        
        //comment this out
        return dStump;
	}
	
	private int getClassValue(CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> example){
		int size=example.size();
		int i;
		int data0=0,data1=0;
        for(i=0;i<size;i++){
            if(example.get(i).get(numOfAttributes)==0){//check class
                data0++;
            }
            else{
                data1++;
            }
        }
        if(data0>=data1){
            return 0;
        }
        else{
            return 1;
        }
	}
	
	private CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> splitData(int val,int index,CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> example){
        CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> splitted=new CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>();
        
        int size=example.size();
        for(int i=0;i<size;i++){
            if(example.get(i).get(index)==val){
                splitted.add(example.get(i));
            }
        }
        return splitted;
    }
	
	private int bestAttribute(double currentEntropy,CopyOnWriteArraySet<Integer> attributes,CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> example){
        int index=0;
        double bestGain=-100;
        double tempGain;
        int exampleSize=example.size();
        for(Integer i:attributes){
            //System.out.println("attrib: "+i);
            tempGain=informationGain(i,exampleSize, currentEntropy,example);
            //System.out.println("gain: "+tempGain);
            if(bestGain<tempGain){
                bestGain=tempGain;
                index=i;
            }
        }
        //System.out.println("bestAttriv: "+index);
        return index;
    }
    
    private double informationGain(int index,int totalData,double currentEntropy,CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> example){//for one atribute
        //,
        //int index=0;
        int i;
        //int totalData;
        //attributes.size();
        int exSize=example.size();
        CopyOnWriteArrayList<Integer> class0=new CopyOnWriteArrayList<Integer>();
        CopyOnWriteArrayList<Integer> class1=new CopyOnWriteArrayList<Integer>();
        for(i=0;i<numOfAttribValues+1;i++){
            class0.add(0);
            class1.add(0);
        }
        
        for(i=0;i<exSize;i++){
            if(example.get(i).get(numOfAttributes)==0){
                class0.set(example.get(i).get(index),class0.get(example.get(i).get(index))+1 ); //0 is j
            }
            else if(example.get(i).get(numOfAttributes)==1){
                class1.set(example.get(i).get(index),class1.get(example.get(i).get(index))+1 ); //0 is j
            }
        }
        //int total=0;
        double gain=0;
        int data0,data1;
        double p1,p2,tempEntropy=0;
        for(i=1;i<numOfAttribValues+1;i++){//value range 1-10
            //System.out.println("i="+i);
            data0=class0.get(i);
            data1=class1.get(i);
            
            //total+=(data0+data1);
            
            //System.out.println("data0="+data0+"\tdata1="+data1);
            if(data0==0 && data1==0){//to make log value 0
                p1=1;
                p2=1;
            }
            else{
                p1=data0/((data0+data1)*1.0);
                p2=data1/((data0+data1)*1.0);
                //System.out.println("p1="+p1+"\tp2="+p2);
                if(p1==0)p1=1;//to calculate log(0)=0 making it log(1) which is 0
                if(p2==0)p2=1;
            }
            tempEntropy+=(((data0+data1)/(totalData*1.0))*(-p1*Math.log(p1)-p2*Math.log(p2)));
        }
        
        gain=currentEntropy-tempEntropy;
        //System.out.println("gain="+gain);
        //System.out.println("totalData="+totalData);
        //System.out.println("total="+total);
        
        return gain;
    }
    
    private double calculateEntropy(CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> example){//information gain for one attribute
        double entropy=0;
        int countPlus=0;
        int exSize=example.size();
        for(int i=0;i<exSize;i++){
            if(example.get(i).get(numOfAttributes)==1)
                countPlus++;
        }
        double p1=countPlus/(exSize*1.0);
        double p2=(exSize-countPlus)/(exSize*1.0);
        entropy=-p1*Math.log(p1)-p2*Math.log(p2);
        //System.out.println("Entropy= "+entropy);
        return entropy;
    }
    
    private void selectTrainAndTestData(int percent){
        int i;
        numOfTrainData= (int)((numOfData*percent)/100);
        numOfTestData=numOfData-numOfTrainData;
        ArrayList<Integer> random=new ArrayList<Integer>();
        for(i=0;i<numOfData;i++){
            random.add(i);
        }
        Collections.shuffle(random);//create array of random integers
        /*
        for(i=0;i<numOfData;i++){
            System.out.print(random.get(i)+" ");
        }
        System.out.println("\n");
        * 
        */
        
        //select train data with first numOfTrainData integers
        for(i=0;i<numOfTrainData;i++){
            //System.out.print(random.get(i)+",");
            trainData.add(data.get(random.get(i)));
        }
        //System.out.println("\n");
        //test data
        for(;i<numOfData;i++){
            //System.out.print(random.get(i)+",");
            testData.add(data.get(random.get(i)));
        }
        //System.out.println("\n"+i);
        
    }
    
    private void test(CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> test, CopyOnWriteArrayList<Double> average){
    	int tp=0;
        int tn=0;
        int fp=0;
        int fn=0;
        
        double accuracy,precision,recall,f_measure,g_mean;
        
    	int size=test.size();
    	int treeSize=treeList.size();
    	//System.out.println("treeSize "+treeSize);
    	
    	for(int i=0;i<size;i++){
    		int classFinal;
    		double classified=0;
    		CopyOnWriteArrayList<Integer> temp=test.get(i);
    		for(int j=0;j<(treeSize);j++){
    			//System.out.println("j: "+j);
    			DecisionStump ds=treeList.get(j);
    			//System.out.println(ds.classVal.get(temp.get(ds.attribute)));
    			//System.out.println("attrib: "+ds.attribute);
    			//System.out.println("j: "+j+" "+ds.classVal);
    			int cv=ds.classVal.get(temp.get(ds.attribute));
    			if(cv==0){
    				//System.out.println("cv=0");
    				classified+=ds.treeWeight*(-1);
    			}
    			else{
    				//System.out.println("cv=1");
    				classified+=ds.treeWeight*(1);
    			}
    		}
    		if(classified>=0){
    			classFinal=1;
    		}
    		else{
    			classFinal=0;
    		}
    		if(temp.get(numOfAttributes)==1 && classFinal== 1){
                tp++;
            }
            else if(temp.get(numOfAttributes)==0 && classFinal== 0){
                tn++;
            }
            else if(temp.get(numOfAttributes)==0 && classFinal== 1){
                fp++;
            }
            else if(temp.get(numOfAttributes)==1 && classFinal== 0){
                fn++;
            }
    	}
    	accuracy=(tp+tn)/((tp+tn+fp+fn)*1.0);
        precision=tp/((tp+fp)*1.0);
        recall=tp/((tp+fn)*1.0);
        f_measure=(2*precision*recall)/(precision+recall);
        g_mean=Math.sqrt(precision*recall);
        
        /*accuracyAvgAB+=accuracy;
        precisionAvgAB+=precision;
        recallAvgAB+=recall;
        f_measureAvgAB+=f_measure;
        g_meanAvgAB+=g_mean;
        */
        
        average.set(0, average.get(0)+accuracy);
        average.set(1, average.get(1)+precision);
        average.set(2, average.get(2)+recall);
        average.set(3, average.get(3)+f_measure);
        average.set(4, average.get(4)+g_mean);
        
        
        /*
        System.out.println("accuracy: "+accuracy);
        System.out.println("precision: "+precision);
        System.out.println("recall: "+recall);
        System.out.println("f_measure: "+f_measure);
        System.out.println("g_mean: "+g_mean);
        */
    }
    
    private void ID3(Node node,CopyOnWriteArraySet<Integer> attributes,CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> example){
        int i,target_attrib;
        int size=example.size();
        if(size==0){//example empty
            node.classval=-1;
            return;
        }
        int val=example.get(0).get(numOfAttributes);
        for(i=1;i<size;i++){
            if(example.get(i).get(numOfAttributes)!=val){//check class
                break;
            }
        }
        if(i==size){//all examples are in same class with value 'val'
            node.classval=val;
            node.child=null;
            return;
        }
        int data0=0,data1=0;
        for(i=0;i<size;i++){
            if(example.get(i).get(numOfAttributes)==0){//check class
                data0++;
            }
            else{
                data1++;
            }
        }
        if(data0>=data1){
            target_attrib=0;
        }
        else{
            target_attrib=1;
        }
        int sizeAtt=attributes.size();
        if(sizeAtt==0){//attributes empty
            //add value for class\
            node.classval=target_attrib;
            node.child=null;
            return;
        }
        
        
        //no termination
        double currentEntropy=calculateEntropy(example);
        int index=bestAttribute(currentEntropy, attributes, example);//decision attribute 
        //System.out.println("bestAttriv: "+index);
        
        CopyOnWriteArraySet<Integer> attributesUpdated=new CopyOnWriteArraySet<Integer>(attributes);
        attributesUpdated.remove(index);
        //System.out.println("After delete attributes:"+attributes);
        //System.out.println("attributesUpdated:"+attributesUpdated);
        
        //update node value
        node.classval=-1;//not a classifier
        node.attribute=index;
        
        //split for the best attribute index
        CopyOnWriteArrayList<CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>> splittedData;
        splittedData=new CopyOnWriteArrayList<CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>>();
        
        for(i=0;i<numOfAttribValues;i++){
            CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> temp;
            temp=splitData(i+1,index,example);
            splittedData.add(temp);
        }
        /*
        for(i=0;i<numOfAttribValues;i++){
            System.out.println(splittedData.get(i));
        }
        * 
        */
        
        for(i=0;i<numOfAttribValues;i++){
            CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> temp;
            temp=splittedData.get(i);
            Node n=new Node();
            n.parent=node;
            node.child.add(n);
            
            if(temp.size()==0){
                //set with target attribute
                n.classval=target_attrib;
                n.child=null;
            }
            else{
                //System.out.println("tempsize:"+temp.size());
                //System.out.println("temp:"+temp);
                ID3(n, attributesUpdated, temp);
            }
            
        }
        
    }

    
    private void testDecisionTree(CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> testData){
        Node currentNode;
        currentNode=root;
        
        int tp=0;
        int tn=0;
        int fp=0;
        int fn=0;
        
        double accuracy,precision,recall,f_measure,g_mean;
        
        for(int i=0;i<numOfTestData;i++){
            CopyOnWriteArrayList<Integer> temp;
            temp=testData.get(i);
            
            //System.out.println(temp);
            while(currentNode.child!=null){
                int index=currentNode.attribute;
                int attribVal=temp.get(index);
                currentNode=currentNode.child.get(attribVal-1);
            }
            //System.out.println(""+currentNode.classval);
            if(temp.get(numOfAttributes)==1 && currentNode.classval== 1){
                tp++;
            }
            else if(temp.get(numOfAttributes)==0 && currentNode.classval== 0){
                tn++;
            }
            else if(temp.get(numOfAttributes)==0 && currentNode.classval== 1){
                fp++;
            }
            else if(temp.get(numOfAttributes)==1 && currentNode.classval== 0){
                fn++;
            }
            
            currentNode=root;
        }
        accuracy=(tp+tn)/((tp+tn+fp+fn)*1.0);
        precision=tp/((tp+fp)*1.0);
        recall=tp/((tp+fn)*1.0);
        f_measure=(2*precision*recall)/(precision+recall);
        g_mean=Math.sqrt(precision*recall);
        
        accuracyAvgDT+=accuracy;
        precisionAvgDT+=precision;
        recallAvgDT+=recall;
        f_measureAvgDT+=f_measure;
        g_meanAvgDT+=g_mean;
        
        //show all
        //System.out.println(accuracy+"\t"+precision+"\t"+recall+"\t"+f_measure+"\t"+g_mean);
        //System.out.printf("%.2f\t%.2f\t%.2f\t%.2f\t%.2f\n",accuracy,precision,recall,f_measure,g_mean);
    }


	public static void main(String[] args) {
		String fileName="//Users//ahmadsabbir//Documents//workspace//DecisionTree//src//data.txt";
		try {
			new AdaBoosting(fileName);
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
		}
	}

}

class DecisionStump{
	int attribute;
	double treeWeight;
	HashMap<Integer,Integer> classVal;
	public DecisionStump(){
		attribute=0;
		treeWeight=0;
		classVal=new HashMap<Integer,Integer>();
	}
	public DecisionStump(int attrib, double weight, HashMap<Integer,Integer> hmap){
		attribute=attrib;
		treeWeight=weight;
		classVal=new HashMap<>(hmap);
	}
}
class Node{
    public int classval=-1;
    public CopyOnWriteArrayList<Node> child=new CopyOnWriteArrayList<Node>();
    public Node parent;
    public int attribute;
}
