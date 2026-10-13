import {
  useQuery,
  useMutation,
  useQueryClient,
  UseQueryOptions,
  UseMutationOptions,
} from '@tanstack/react-query';

export function useApiQuery<T>(
  key: string[],
  queryFn: () => Promise<T>,
  options?: Partial<UseQueryOptions<T, Error>>
) {
  return useQuery<T, Error>({
    queryKey: key,
    queryFn,
    ...options,
  });
}

export function useApiMutation<T, TVariables>(
  key: string[],
  mutationFn: (variables: TVariables) => Promise<T>,
  options?: Partial<UseMutationOptions<T, Error, TVariables>>
) {
  const queryClient = useQueryClient();

  return useMutation<T, Error, TVariables>({
    ...options,
    mutationFn,
    onSuccess: (data, variables, context) => {
      queryClient.invalidateQueries({ queryKey: key });
      (options?.onSuccess as any)?.(data, variables, context);
    },
  });
}

export function useInvalidateQuery(key: string[]) {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: key });
}

export function useSetQueryData<T>(key: string[]) {
  const queryClient = useQueryClient();
  return (data: T) => queryClient.setQueryData(key, data);
}

export function useGetQueryData<T>(key: string[]) {
  const queryClient = useQueryClient();
  return () => queryClient.getQueryData<T>(key);
}
