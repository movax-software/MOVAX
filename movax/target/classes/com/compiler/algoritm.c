#include <stdio.h>

void bubbleSort(int nums[], int length);
void quickSort(int nums[], int inicio, int fin);

int main(){
    int Arr1[] = {2,1,3,0,5,4,9,3};
    int length = sizeof(Arr1) / sizeof(Arr1[0]);

    printf("\nBUBBLESORT\n\n");
    
    for (int i = 0; i < length; i++) 
        printf("Elemento desordenado: %d: %d\n", i, Arr1[i]);

    bubbleSort(Arr1, length);

    for (int i = 0; i < length; i++) 
        printf("Elemento ordenado: %d: %d\n", i, Arr1[i]);
        
    int Arr2[] = {2,1,3,0,5,4,9,3};

    printf("\nQUICKSORT\n\n");
     
    for (int i = 0; i < length; i++) 
        printf("Elemento desordenado: %d: %d\n", i, Arr2[i]);

    quickSort(Arr2, 0, length - 1);

    for (int i = 0; i < length; i++) 
        printf("Elemento ordenado: %d: %d\n", i, Arr2[i]);
    
    
    return 0;
}

void bubbleSort(int nums[], int length){    
    
    for (int i = 0; i < length-1; i++)
    for (int j = 0; j < length-1; j++) {
        if (nums[j] > nums[j+1]) {
            int temp = nums[j];
            nums[j] = nums[j+1];
            nums[j+1] = temp;
        }
    } 

}

void quickSort(int nums[], int inicio, int fin) {
    if (inicio >= fin) 
        return;
    
    int i = inicio - 1;
    int pivote = nums[fin];

    for (int j = inicio; j < fin; j++) {
        if (nums[j] <= pivote) {
            i++;
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
        }
    }
    
    int indicePivote = i + 1;
    int temp = nums[indicePivote];
    nums[indicePivote]  = nums[fin];
    nums[fin] = temp;

    quickSort(nums, inicio, indicePivote - 1);
    quickSort(nums, indicePivote + 1, fin);
}
